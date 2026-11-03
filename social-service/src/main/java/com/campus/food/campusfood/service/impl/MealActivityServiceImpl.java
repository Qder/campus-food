package com.campus.food.campusfood.service.impl;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campus.food.campusfood.common.UserContext;
import com.campus.food.campusfood.dto.CancelMealActivityDTO;
import com.campus.food.campusfood.dto.CreateMealActivityDTO;
import com.campus.food.campusfood.dto.SubmitMealFeedbackDTO;
import com.campus.food.campusfood.entity.FoodItem;
import com.campus.food.campusfood.entity.MealActivity;
import com.campus.food.campusfood.entity.MealActivityFeedback;
import com.campus.food.campusfood.entity.MealActivityMember;
import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.feign.UserFeignClient;
import com.campus.food.campusfood.feign.UserInfo;
import com.campus.food.campusfood.mapper.FoodItemMapper;
import com.campus.food.campusfood.mapper.MealActivityFeedbackMapper;
import com.campus.food.campusfood.mapper.MealActivityMapper;
import com.campus.food.campusfood.mapper.MealActivityMemberMapper;
import com.campus.food.campusfood.service.MealActivityService;
import com.campus.food.campusfood.vo.MealActivityCreateVO;
import com.campus.food.campusfood.vo.MealActivityMemberVO;
import com.campus.food.campusfood.vo.MealActivityVO;
import com.campus.food.campusfood.vo.MealCancelResultVO;
import com.campus.food.campusfood.vo.MealFeedbackCreateVO;
import com.campus.food.campusfood.vo.MealFeedbackVO;
import com.campus.food.campusfood.vo.MealJoinResultVO;
import com.campus.food.campusfood.vo.MealRemoveResultVO;
import com.campus.food.campusfood.vo.PageVO;

import feign.FeignException;

/**
 * 拼单约饭实现。MySQL 三张表；昵称走 user-service，菜名走同库 {@code food_item}。
 *
 * @author yang
 * @since 2026-11-02
 */
@Service
public class MealActivityServiceImpl implements MealActivityService {

    private static final Logger log = LoggerFactory.getLogger(MealActivityServiceImpl.class);

    /** 0 报名中 */
    private static final int STATUS_SIGNUP = 0;
    /** 1 已满 */
    private static final int STATUS_FULL = 1;
    /** 2 进行中 */
    private static final int STATUS_ONGOING = 2;
    /** 3 已结束 */
    private static final int STATUS_ENDED = 3;
    /** 4 已取消 */
    private static final int STATUS_CANCELLED = 4;

    /** 成员：已报名 */
    private static final int MEMBER_JOINED = 1;
    /** 成员：已退出 */
    private static final int MEMBER_QUIT = 2;
    /** 成员：被移除 */
    private static final int MEMBER_REMOVED = 3;

    /** 0 普通成员 / 1 发起人 */
    private static final int ROLE_MEMBER = 0;
    private static final int ROLE_CREATOR = 1;

    /** 登录角色：学生用户 */
    private static final int ROLE_USER = 1;

    private static final int TITLE_MAX = 128;
    private static final int PLACE_MAX = 255;
    private static final int FOOD_TYPE_MAX = 64;
    private static final int DESC_MAX = 1000;
    private static final int REASON_MAX = 255;
    private static final int CONTENT_MAX = 500;
    private static final int MIN_MEMBERS = 2;
    private static final int MAX_MEMBERS = 50;
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;

    /** 活动时间按北京时间落库、比较 */
    private static final ZoneId ZONE_SHANGHAI = ZoneId.of("Asia/Shanghai");

    private final MealActivityMapper mealActivityMapper;
    private final MealActivityMemberMapper mealActivityMemberMapper;
    private final MealActivityFeedbackMapper mealActivityFeedbackMapper;
    private final FoodItemMapper foodItemMapper;
    private final UserFeignClient userFeignClient;

    /**
     * @param mealActivityMapper         活动表
     * @param mealActivityMemberMapper   报名表
     * @param mealActivityFeedbackMapper 反馈表
     * @param foodItemMapper             同库菜名
     * @param userFeignClient            昵称头像
     */
    public MealActivityServiceImpl(MealActivityMapper mealActivityMapper,
                                   MealActivityMemberMapper mealActivityMemberMapper,
                                   MealActivityFeedbackMapper mealActivityFeedbackMapper,
                                   FoodItemMapper foodItemMapper,
                                   UserFeignClient userFeignClient) {
        this.mealActivityMapper = mealActivityMapper;
        this.mealActivityMemberMapper = mealActivityMemberMapper;
        this.mealActivityFeedbackMapper = mealActivityFeedbackMapper;
        this.foodItemMapper = foodItemMapper;
        this.userFeignClient = userFeignClient;
    }

    /**
     * {@inheritDoc}
     *
     * <p>未登录也可翻列表。{@code mine}/{@code joined} 为 true 时必须已登录。
     * 每条先做 meetTime 懒更新，再填 {@code joined}/{@code isCreator}。
     */
    @Override
    public PageVO<MealActivityVO> list(Integer status, String foodType, Boolean mine, Boolean joined,
                                       Integer page, Integer size) {
        if (status != null && (status < STATUS_SIGNUP || status > STATUS_CANCELLED)) {
            throw new BizException("status 取值不合法");
        }
        Long curId = UserContext.getUserId();
        Long creatorId = null;
        Long joinedUserId = null;
        if (Boolean.TRUE.equals(mine)) {
            creatorId = requireCurUserId();
        }
        if (Boolean.TRUE.equals(joined)) {
            joinedUserId = requireCurUserId();
        }
        int p = (page == null || page < 1) ? DEFAULT_PAGE : page;
        int s = (size == null || size < 1) ? DEFAULT_SIZE : Math.min(size, MAX_PAGE_SIZE);
        String type = blankToNull(foodType);

        int total = mealActivityMapper.countByFilter(status, type, creatorId, joinedUserId);
        List<MealActivity> rows = mealActivityMapper.selectByFilter(
                status, type, creatorId, joinedUserId, (p - 1) * s, s);

        Map<Long, String> nickCache = new HashMap<>();
        List<MealActivityVO> list = new ArrayList<>();
        for (MealActivity row : rows) {
            refreshIfDue(row);
            list.add(toListVo(row, curId, nickCache));
        }

        PageVO<MealActivityVO> pageVO = new PageVO<>();
        pageVO.setTotal(total);
        pageVO.setPage(p);
        pageVO.setSize(s);
        pageVO.setList(list);
        return pageVO;
    }

    /**
     * {@inheritDoc}
     *
     * <p>校验标题/时间/人数后插入活动 {@code status=0}，再插发起人成员 {@code role=1}，人数从 1 起。
     */
    @Override
    @Transactional
    public MealActivityCreateVO create(CreateMealActivityDTO dto) {
        Long curId = requireCurUserId();
        if (dto == null) {
            throw new BizException("活动信息不能为空");
        }
        String title = requireLength(dto.getTitle(), 1, TITLE_MAX, "活动名称不能为空", "活动名称不能超过 128 字");
        String place = requireLength(dto.getMeetPlace(), 1, PLACE_MAX, "活动地点不能为空", "活动地点不能超过 255 字");
        if (dto.getMeetTime() == null) {
            throw new BizException("活动时间不能为空");
        }
        if (!dto.getMeetTime().isAfter(OffsetDateTime.now())) {
            throw new BizException("活动时间必须晚于当前时间");
        }
        LocalDateTime meetTime = toShanghai(dto.getMeetTime());
        LocalDateTime deadline = null;
        if (dto.getSignupDeadline() != null) {
            if (!dto.getSignupDeadline().isBefore(dto.getMeetTime())) {
                throw new BizException("报名截止时间必须早于活动时间");
            }
            deadline = toShanghai(dto.getSignupDeadline());
        }
        Integer max = dto.getMaxMembers();
        if (max == null || max < MIN_MEMBERS || max > MAX_MEMBERS) {
            throw new BizException("人数上限须为 2–50");
        }
        String foodType = optionalLength(dto.getFoodType(), FOOD_TYPE_MAX, "美食类型不能超过 64 字");
        String description = optionalLength(dto.getDescription(), DESC_MAX, "说明不能超过 1000 字");

        MealActivity row = new MealActivity();
        row.setCreatorId(curId);
        row.setTitle(title);
        row.setMeetTime(meetTime);
        row.setMeetPlace(place);
        row.setFoodType(foodType);
        row.setRelatedFoodId(dto.getRelatedFoodId());
        row.setRelatedMerchantId(dto.getRelatedMerchantId());
        row.setMaxMembers(max);
        row.setCurrentMembers(1);
        row.setDescription(description);
        row.setSignupDeadline(deadline);
        row.setStatus(STATUS_SIGNUP);
        mealActivityMapper.insert(row);

        MealActivityMember creator = new MealActivityMember();
        creator.setActivityId(row.getId());
        creator.setUserId(curId);
        creator.setRole(ROLE_CREATOR);
        creator.setStatus(MEMBER_JOINED);
        mealActivityMemberMapper.insert(creator);

        MealActivity saved = mealActivityMapper.selectById(row.getId());
        MealActivityCreateVO vo = new MealActivityCreateVO();
        vo.setActivityId(saved.getId());
        vo.setStatus(saved.getStatus());
        vo.setCurrentMembers(saved.getCurrentMembers());
        vo.setCreatedAt(saved.getCreatedAt());
        return vo;
    }

    /**
     * {@inheritDoc}
     *
     * <p>已取消当不存在。成员只带 {@code status=1}；菜名同库查 {@code food_item}。
     */
    @Override
    public MealActivityVO getDetail(Long activityId) {
        MealActivity row = loadVisible(activityId);
        refreshIfDue(row);
        if (row.getStatus() != null && row.getStatus() == STATUS_CANCELLED) {
            throw new BizException("活动不存在或已取消");
        }
        Long curId = UserContext.getUserId();
        Map<Long, String> nickCache = new HashMap<>();
        MealActivityVO vo = toListVo(row, curId, nickCache);
        vo.setDescription(row.getDescription());
        vo.setRelatedFoodId(row.getRelatedFoodId());
        vo.setRelatedMerchantId(row.getRelatedMerchantId());
        vo.setRelatedFoodName(loadFoodName(row.getRelatedFoodId()));

        List<MealActivityMemberVO> members = new ArrayList<>();
        Map<Long, UserInfo> userCache = new HashMap<>();
        for (MealActivityMember member : mealActivityMemberMapper.selectJoinedByActivity(row.getId())) {
            members.add(toMemberVo(member, userCache, false));
        }
        vo.setMembers(members);
        return vo;
    }

    /**
     * {@inheritDoc}
     *
     * <p>先校验报名中/未满/未截止，再 {@code tryIncrementMembers} 占名额。
     * 曾退出或被移除的人改回 {@code status=1}，不插新行（唯一键是活动+用户）。
     */
    @Override
    @Transactional
    public MealJoinResultVO join(Long activityId) {
        Long curId = requireStudentUser("只有学生可以报名参加");
        MealActivity row = loadVisible(activityId);
        refreshIfDue(row);
        if (row.getCreatorId().equals(curId)) {
            throw new BizException("不能报名自己发起的活动");
        }
        if (row.getStatus() != null && row.getStatus() == STATUS_CANCELLED) {
            throw new BizException("活动不存在或已取消");
        }
        if (row.getStatus() != null && row.getStatus() >= STATUS_ONGOING) {
            throw new BizException("活动已开始，无法报名");
        }
        if (row.getStatus() != null && row.getStatus() == STATUS_FULL) {
            throw new BizException("活动已满员");
        }
        if (row.getSignupDeadline() != null && !LocalDateTime.now().isBefore(row.getSignupDeadline())) {
            throw new BizException("报名已截止");
        }
        if (row.getStatus() == null || row.getStatus() != STATUS_SIGNUP) {
            throw new BizException("活动已满员");
        }

        MealActivityMember existing = mealActivityMemberMapper.selectByActivityAndUser(row.getId(), curId);
        if (existing != null && existing.getStatus() != null && existing.getStatus() == MEMBER_JOINED) {
            throw new BizException("你已报名该活动");
        }
        int changed = mealActivityMapper.tryIncrementMembers(row.getId());
        if (changed == 0) {
            throw new BizException("活动已满员");
        }
        if (existing != null) {
            mealActivityMemberMapper.updateStatus(existing.getId(), MEMBER_JOINED);
        } else {
            MealActivityMember member = new MealActivityMember();
            member.setActivityId(row.getId());
            member.setUserId(curId);
            member.setRole(ROLE_MEMBER);
            member.setStatus(MEMBER_JOINED);
            mealActivityMemberMapper.insert(member);
        }

        MealActivity latest = mealActivityMapper.selectById(row.getId());
        MealJoinResultVO vo = new MealJoinResultVO();
        vo.setActivityId(latest.getId());
        vo.setJoined(true);
        vo.setCurrentMembers(latest.getCurrentMembers());
        vo.setStatus(latest.getStatus());
        return vo;
    }

    /**
     * {@inheritDoc}
     *
     * <p>发起人不能退出。活动已开始（status≥2）不能退。成员改 {@code status=2}，人数 -1。
     */
    @Override
    @Transactional
    public MealJoinResultVO quit(Long activityId) {
        Long curId = requireStudentUser("只有学生可以退出拼单");
        MealActivity row = loadVisible(activityId);
        refreshIfDue(row);
        if (row.getStatus() != null && row.getStatus() >= STATUS_ONGOING) {
            throw new BizException("活动已开始，无法退出");
        }
        MealActivityMember member = mealActivityMemberMapper.selectByActivityAndUser(row.getId(), curId);
        if (member != null && member.getRole() != null && member.getRole() == ROLE_CREATOR) {
            throw new BizException("发起人不能退出，请取消活动");
        }
        if (member == null || member.getStatus() == null || member.getStatus() != MEMBER_JOINED) {
            throw new BizException("你尚未报名该活动");
        }
        mealActivityMemberMapper.updateStatus(member.getId(), MEMBER_QUIT);
        mealActivityMapper.decrementMembers(row.getId());

        MealActivity latest = mealActivityMapper.selectById(row.getId());
        MealJoinResultVO vo = new MealJoinResultVO();
        vo.setActivityId(latest.getId());
        vo.setJoined(false);
        vo.setCurrentMembers(latest.getCurrentMembers());
        return vo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MealActivityMemberVO> listMembers(Long activityId) {
        Long curId = requireCurUserId();
        MealActivity row = loadVisible(activityId);
        refreshIfDue(row);
        if (!row.getCreatorId().equals(curId)) {
            throw new BizException("只有发起人可以管理名单");
        }
        Map<Long, UserInfo> userCache = new HashMap<>();
        List<MealActivityMemberVO> list = new ArrayList<>();
        for (MealActivityMember member : mealActivityMemberMapper.selectAllByActivity(row.getId())) {
            list.add(toMemberVo(member, userCache, true));
        }
        return list;
    }

    /**
     * {@inheritDoc}
     *
     * <p>已结束/已取消不能改名单。被移出的人 {@code status=3}，人数 -1。
     */
    @Override
    @Transactional
    public MealRemoveResultVO removeMember(Long activityId, Long userId) {
        Long curId = requireCurUserId();
        MealActivity row = loadVisible(activityId);
        refreshIfDue(row);
        if (!row.getCreatorId().equals(curId)) {
            throw new BizException("只有发起人可以管理名单");
        }
        if (row.getStatus() != null && row.getStatus() >= STATUS_ENDED) {
            throw new BizException("活动已结束，无法调整名单");
        }
        if (userId == null) {
            throw new BizException("用户ID不能为空");
        }
        if (userId.equals(curId)) {
            throw new BizException("不能移出发起人");
        }
        MealActivityMember member = mealActivityMemberMapper.selectByActivityAndUser(row.getId(), userId);
        if (member == null || member.getStatus() == null) {
            throw new BizException("该成员不在名单中");
        }
        if (member.getStatus() == MEMBER_QUIT) {
            throw new BizException("该成员已退出，无需移出");
        }
        if (member.getStatus() == MEMBER_REMOVED) {
            throw new BizException("该成员已被移出");
        }
        if (member.getStatus() != MEMBER_JOINED) {
            throw new BizException("该成员不在名单中");
        }
        mealActivityMemberMapper.updateStatus(member.getId(), MEMBER_REMOVED);
        mealActivityMapper.decrementMembers(row.getId());

        MealActivity latest = mealActivityMapper.selectById(row.getId());
        MealRemoveResultVO vo = new MealRemoveResultVO();
        vo.setActivityId(latest.getId());
        vo.setUserId(userId);
        vo.setCurrentMembers(latest.getCurrentMembers());
        return vo;
    }

    /**
     * {@inheritDoc}
     *
     * <p>原因只校验长度，表没有 reason 列。取消后 status=4。
     */
    @Override
    @Transactional
    public MealCancelResultVO cancel(Long activityId, CancelMealActivityDTO dto) {
        Long curId = requireCurUserId();
        MealActivity row = loadVisible(activityId);
        refreshIfDue(row);
        if (!row.getCreatorId().equals(curId)) {
            throw new BizException("只有发起人可以取消活动");
        }
        if (row.getStatus() != null && row.getStatus() >= STATUS_ENDED) {
            throw new BizException("活动已结束，无需取消");
        }
        if (dto != null) {
            optionalLength(dto.getReason(), REASON_MAX, "取消原因不能超过 255 字");
        }
        mealActivityMapper.updateStatus(row.getId(), STATUS_CANCELLED);

        MealCancelResultVO vo = new MealCancelResultVO();
        vo.setActivityId(row.getId());
        vo.setStatus(STATUS_CANCELLED);
        return vo;
    }

    /**
     * {@inheritDoc}
     *
     * <p>仅 {@code status=3} 且本人仍是已报名成员。同一人对同一对象（含活动整体）只评一次。
     */
    @Override
    @Transactional
    public MealFeedbackCreateVO submitFeedback(Long activityId, SubmitMealFeedbackDTO dto) {
        Long curId = requireStudentUser("只有学生可以评价这次约饭");
        MealActivity row = loadVisible(activityId);
        refreshIfDue(row);
        if (row.getStatus() == null || row.getStatus() != STATUS_ENDED) {
            throw new BizException("活动尚未结束，暂不能评价");
        }
        requireJoinedMember(row.getId(), curId);
        if (dto == null || dto.getScore() == null || dto.getScore() < 1 || dto.getScore() > 5) {
            throw new BizException("评分须为 1–5");
        }
        Long toUserId = dto.getToUserId();
        if (toUserId != null) {
            if (toUserId.equals(curId)) {
                throw new BizException("不能评价自己");
            }
            MealActivityMember target = mealActivityMemberMapper.selectByActivityAndUser(row.getId(), toUserId);
            if (target == null || target.getStatus() == null || target.getStatus() != MEMBER_JOINED) {
                throw new BizException("被评价人不是活动成员");
            }
        }
        if (mealActivityFeedbackMapper.countByFromTo(row.getId(), curId, toUserId) > 0) {
            throw new BizException("你已评价过");
        }
        String content = optionalLength(dto.getContent(), CONTENT_MAX, "反馈内容不能超过 500 字");

        MealActivityFeedback feedback = new MealActivityFeedback();
        feedback.setActivityId(row.getId());
        feedback.setFromUserId(curId);
        feedback.setToUserId(toUserId);
        feedback.setScore(dto.getScore());
        feedback.setContent(content);
        mealActivityFeedbackMapper.insert(feedback);

        MealFeedbackCreateVO vo = new MealFeedbackCreateVO();
        vo.setFeedbackId(feedback.getId());
        vo.setActivityId(row.getId());
        return vo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MealFeedbackVO> listFeedback(Long activityId) {
        Long curId = requireCurUserId();
        MealActivity row = loadVisible(activityId);
        refreshIfDue(row);
        requireJoinedMember(row.getId(), curId);
        Map<Long, String> nickCache = new HashMap<>();
        List<MealFeedbackVO> list = new ArrayList<>();
        for (MealActivityFeedback rowFb : mealActivityFeedbackMapper.selectByActivityId(row.getId())) {
            MealFeedbackVO vo = new MealFeedbackVO();
            vo.setFeedbackId(rowFb.getId());
            vo.setFromUserId(rowFb.getFromUserId());
            vo.setFromNickname(nicknameOf(rowFb.getFromUserId(), nickCache));
            vo.setToUserId(rowFb.getToUserId());
            vo.setScore(rowFb.getScore());
            vo.setContent(rowFb.getContent());
            vo.setCreatedAt(rowFb.getCreatedAt());
            list.add(vo);
        }
        return list;
    }

    /**
     * 到点懒更新：报名中/已满 → 进行中 → 已结束。已取消不动。无定时任务。
     */
    private void refreshIfDue(MealActivity row) {
        if (row == null || row.getMeetTime() == null || row.getStatus() == null) {
            return;
        }
        if (row.getStatus() == STATUS_CANCELLED || row.getStatus() == STATUS_ENDED) {
            return;
        }
        if (LocalDateTime.now().isBefore(row.getMeetTime())) {
            return;
        }
        if (row.getStatus() == STATUS_SIGNUP || row.getStatus() == STATUS_FULL) {
            mealActivityMapper.updateStatus(row.getId(), STATUS_ONGOING);
            row.setStatus(STATUS_ONGOING);
        }
        if (row.getStatus() == STATUS_ONGOING) {
            mealActivityMapper.updateStatus(row.getId(), STATUS_ENDED);
            row.setStatus(STATUS_ENDED);
        }
    }

    /** 活动必须存在；取消的详情/报名当不存在，列表仍可能带 status=4。 */
    private MealActivity loadVisible(Long activityId) {
        if (activityId == null) {
            throw new BizException("活动不存在或已取消");
        }
        MealActivity row = mealActivityMapper.selectById(activityId);
        if (row == null) {
            throw new BizException("活动不存在或已取消");
        }
        return row;
    }

    private MealActivityVO toListVo(MealActivity row, Long curId, Map<Long, String> nickCache) {
        MealActivityVO vo = new MealActivityVO();
        vo.setActivityId(row.getId());
        vo.setTitle(row.getTitle());
        vo.setCreatorId(row.getCreatorId());
        vo.setCreatorNickname(nicknameOf(row.getCreatorId(), nickCache));
        vo.setMeetTime(row.getMeetTime());
        vo.setMeetPlace(row.getMeetPlace());
        vo.setFoodType(row.getFoodType());
        vo.setMaxMembers(row.getMaxMembers());
        vo.setCurrentMembers(row.getCurrentMembers());
        vo.setSignupDeadline(row.getSignupDeadline());
        vo.setStatus(row.getStatus());
        boolean isCreator = curId != null && curId.equals(row.getCreatorId());
        vo.setIsCreator(isCreator);
        vo.setJoined(isCreator || isJoined(row.getId(), curId));
        return vo;
    }

    private MealActivityMemberVO toMemberVo(MealActivityMember member,
                                            Map<Long, UserInfo> userCache,
                                            boolean withJoinedAt) {
        MealActivityMemberVO vo = new MealActivityMemberVO();
        vo.setUserId(member.getUserId());
        vo.setRole(member.getRole());
        vo.setStatus(member.getStatus());
        if (withJoinedAt) {
            vo.setJoinedAt(member.getJoinedAt());
        }
        UserInfo info = userCache.computeIfAbsent(member.getUserId(), this::loadUserQuiet);
        if (info != null) {
            vo.setNickname(info.getNickname() != null ? info.getNickname() : "用户");
            vo.setAvatarUrl(info.getAvatarUrl());
        } else {
            vo.setNickname("用户");
        }
        return vo;
    }

    private boolean isJoined(Long activityId, Long userId) {
        if (userId == null) {
            return false;
        }
        MealActivityMember member = mealActivityMemberMapper.selectByActivityAndUser(activityId, userId);
        return member != null && member.getStatus() != null && member.getStatus() == MEMBER_JOINED;
    }

    private void requireJoinedMember(Long activityId, Long userId) {
        MealActivityMember member = mealActivityMemberMapper.selectByActivityAndUser(activityId, userId);
        if (member == null || member.getStatus() == null || member.getStatus() != MEMBER_JOINED) {
            throw new BizException("只有参与成员可以评价");
        }
    }

    private String loadFoodName(Long foodId) {
        if (foodId == null) {
            return null;
        }
        FoodItem item = foodItemMapper.selectById(foodId);
        return item == null ? null : item.getName();
    }

    private String nicknameOf(Long userId, Map<Long, String> cache) {
        if (userId == null) {
            return "用户";
        }
        if (cache.containsKey(userId)) {
            return cache.get(userId);
        }
        UserInfo info = loadUserQuiet(userId);
        String nick = (info != null && info.getNickname() != null && !info.getNickname().isBlank())
                ? info.getNickname() : "用户";
        cache.put(userId, nick);
        return nick;
    }

    private UserInfo loadUserQuiet(Long userId) {
        try {
            UserInfo user = userFeignClient.getUserInfo(userId);
            if (user == null || user.getId() == null) {
                return null;
            }
            return user;
        } catch (FeignException | BizException e) {
            log.warn("查询用户失败，已跳过, userId={}", userId);
            return null;
        }
    }

    private Long requireCurUserId() {
        Long curId = UserContext.getUserId();
        if (curId == null) {
            throw new BizException("请先登录");
        }
        return curId;
    }

    /**
     * 报名/退出/评价：契约只允许学生（{@code roleId=1}）。
     * 商家、管理员用对应接口自己的文案，避免一句「无权访问」说不清。
     */
    private Long requireStudentUser(String message) {
        Long curId = requireCurUserId();
        Integer roleId = UserContext.getRoleId();
        if (roleId == null || roleId != ROLE_USER) {
            throw new BizException(message);
        }
        return curId;
    }

    /** 带时区的时间转到北京时间再写入 DATETIME，避免 UTC 墙钟被当成北京时间。 */
    private static LocalDateTime toShanghai(OffsetDateTime value) {
        return value.atZoneSameInstant(ZONE_SHANGHAI).toLocalDateTime();
    }

    private static String requireLength(String raw, int min, int max, String emptyMsg, String tooLongMsg) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new BizException(emptyMsg);
        }
        String text = raw.trim();
        if (text.length() < min) {
            throw new BizException(emptyMsg);
        }
        if (text.length() > max) {
            throw new BizException(tooLongMsg);
        }
        return text;
    }

    private static String optionalLength(String raw, int max, String tooLongMsg) {
        if (raw == null) {
            return null;
        }
        String text = raw.trim();
        if (text.isEmpty()) {
            return null;
        }
        if (text.length() > max) {
            throw new BizException(tooLongMsg);
        }
        return text;
    }

    private static String blankToNull(String raw) {
        if (raw == null) {
            return null;
        }
        String text = raw.trim();
        return text.isEmpty() ? null : text;
    }
}
