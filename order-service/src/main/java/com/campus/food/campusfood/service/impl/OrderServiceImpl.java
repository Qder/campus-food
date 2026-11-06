package com.campus.food.campusfood.service.impl;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.campus.food.campusfood.common.UserContext;
import com.campus.food.campusfood.dto.OrderLineDTO;
import com.campus.food.campusfood.dto.PlaceOrderDTO;
import com.campus.food.campusfood.entity.FoodItem;
import com.campus.food.campusfood.entity.FoodOrder;
import com.campus.food.campusfood.entity.Merchant;
import com.campus.food.campusfood.entity.OrderItem;
import com.campus.food.campusfood.entity.PaymentRecord;
import com.campus.food.campusfood.enums.FoodItemStatus;
import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.mapper.FoodItemMapper;
import com.campus.food.campusfood.mapper.FoodOrderMapper;
import com.campus.food.campusfood.mapper.MerchantMapper;
import com.campus.food.campusfood.mapper.OrderItemMapper;
import com.campus.food.campusfood.mapper.PaymentRecordMapper;
import com.campus.food.campusfood.service.OrderService;
import com.campus.food.campusfood.vo.OrderPlaceVO;

/**
 * 订单业务。本期只实现详情页下单并 MOCK 支付。
 *
 * @author yang
 * @since 2026-11-05
 */
@Service
public class OrderServiceImpl implements OrderService {

    private static final int ROLE_USER = 1;

    private static final int MERCHANT_AUDIT_PASS = 1;
    private static final int MERCHANT_OPEN = 1;

    private static final String STATUS_PAID = "PAID";
    private static final String PAY_MOCK = "MOCK";
    private static final int PAY_STATUS_SUCCESS = 1;

    private static final int DELIVERY_PICKUP = 1;
    private static final int DELIVERY_TO_DORM = 2;

    private static final int MAX_QTY = 99;
    private static final int MAX_REMARK_LEN = 255;
    private static final int MAX_ADDRESS_LEN = 255;

    private static final String IDEM_KEY_PREFIX = "order:idem:";

    private final FoodOrderMapper foodOrderMapper;
    private final OrderItemMapper orderItemMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final FoodItemMapper foodItemMapper;
    private final MerchantMapper merchantMapper;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * @param foodOrderMapper       订单主表
     * @param orderItemMapper       明细
     * @param paymentRecordMapper   支付记录
     * @param foodItemMapper        菜品（扣库存）
     * @param merchantMapper        店铺营业状态
     * @param stringRedisTemplate   幂等令牌
     */
    @Autowired
    public OrderServiceImpl(FoodOrderMapper foodOrderMapper,
                            OrderItemMapper orderItemMapper,
                            PaymentRecordMapper paymentRecordMapper,
                            FoodItemMapper foodItemMapper,
                            MerchantMapper merchantMapper,
                            StringRedisTemplate stringRedisTemplate) {
        this.foodOrderMapper = foodOrderMapper;
        this.orderItemMapper = orderItemMapper;
        this.paymentRecordMapper = paymentRecordMapper;
        this.foodItemMapper = foodItemMapper;
        this.merchantMapper = merchantMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * {@inheritDoc}
     *
     * <p>校验菜品与店铺后扣库存，写主表/明细/支付记录，MOCK 直接 {@code PAID}。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderPlaceVO placeOrder(PlaceOrderDTO dto) {
        Long userId = requireStudentUser("只有学生可以下单");
        validatePlaceOrder(dto);

        Map<Long, Integer> merged = mergeLines(dto.getItems());
        List<ResolvedLine> lines = resolveLines(merged);
        Long merchantId = lines.get(0).food.getMerchantId();
        for (ResolvedLine line : lines) {
            if (!merchantId.equals(line.food.getMerchantId())) {
                throw new BizException("请分开结算不同店铺的菜品");
            }
        }

        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null || (merchant.getDeleted() != null && merchant.getDeleted() != 0)) {
            throw new BizException("店铺不存在");
        }
        if (merchant.getAuditStatus() == null || merchant.getAuditStatus() != MERCHANT_AUDIT_PASS) {
            throw new BizException("店铺尚未通过审核，无法下单");
        }
        if (merchant.getStatus() == null || merchant.getStatus() != MERCHANT_OPEN) {
            throw new BizException("店铺休息中，暂不可下单");
        }

        assertIdempotent(userId, dto.getClientOrderToken());

        BigDecimal total = BigDecimal.ZERO;
        for (ResolvedLine line : lines) {
            if (foodItemMapper.tryDecrementStock(line.food.getId(), line.quantity) != 1) {
                throw new BizException("「" + line.food.getName() + "」库存不足，下单失败");
            }
            total = total.add(line.amount);
        }

        LocalDateTime now = LocalDateTime.now();
        String orderNo = buildOrderNo(userId);

        FoodOrder order = new FoodOrder();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setMerchantId(merchantId);
        order.setTotalAmount(total);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPayAmount(total);
        order.setStatus(STATUS_PAID);
        order.setDeliveryType(dto.getDeliveryType());
        order.setDeliveryAddress(blankToNull(dto.getDeliveryAddress()));
        order.setRemark(blankToNull(dto.getRemark()));
        order.setPaidAt(now);
        foodOrderMapper.insert(order);

        for (ResolvedLine line : lines) {
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setFoodId(line.food.getId());
            item.setFoodName(line.food.getName());
            item.setUnitPrice(line.food.getPrice());
            item.setQuantity(line.quantity);
            item.setAmount(line.amount);
            orderItemMapper.insert(item);
        }

        PaymentRecord pay = new PaymentRecord();
        pay.setOrderId(order.getId());
        pay.setOrderNo(orderNo);
        pay.setPayChannel(PAY_MOCK);
        pay.setTradeNo("MOCK-" + orderNo);
        pay.setAmount(total);
        pay.setStatus(PAY_STATUS_SUCCESS);
        pay.setPaidAt(now);
        paymentRecordMapper.insert(pay);

        OrderPlaceVO vo = new OrderPlaceVO();
        vo.setOrderId(order.getId());
        vo.setOrderNo(orderNo);
        vo.setStatus(STATUS_PAID);
        vo.setPayAmount(total);
        return vo;
    }

    private void validatePlaceOrder(PlaceOrderDTO dto) {
        if (dto == null || dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BizException("请至少选择一件商品");
        }
        if (dto.getDeliveryType() == null
                || (dto.getDeliveryType() != DELIVERY_PICKUP && dto.getDeliveryType() != DELIVERY_TO_DORM)) {
            throw new BizException("deliveryType 取值不合法");
        }
        if (dto.getDeliveryType() == DELIVERY_TO_DORM) {
            if (!StringUtils.hasText(dto.getDeliveryAddress())
                    || dto.getDeliveryAddress().length() > MAX_ADDRESS_LEN) {
                throw new BizException("配送到寝必须填写收货地址");
            }
        }
        if (dto.getRemark() != null && dto.getRemark().length() > MAX_REMARK_LEN) {
            throw new BizException("备注不能超过 255 字");
        }
        if (!StringUtils.hasText(dto.getClientOrderToken())) {
            throw new BizException("缺少 clientOrderToken");
        }
        if (!PAY_MOCK.equals(dto.getPayChannel())) {
            throw new BizException("暂不支持该支付方式");
        }
    }

    private Map<Long, Integer> mergeLines(List<OrderLineDTO> items) {
        Map<Long, Integer> merged = new LinkedHashMap<>();
        for (OrderLineDTO line : items) {
            if (line == null || line.getFoodId() == null || line.getQuantity() == null) {
                throw new BizException("商品或数量不能为空");
            }
            if (line.getQuantity() < 1 || line.getQuantity() > MAX_QTY) {
                throw new BizException("数量须在 1–99 之间");
            }
            merged.merge(line.getFoodId(), line.getQuantity(), Integer::sum);
            if (merged.get(line.getFoodId()) > MAX_QTY) {
                throw new BizException("同一菜品单次最多购买 99 份");
            }
        }
        return merged;
    }

    private List<ResolvedLine> resolveLines(Map<Long, Integer> merged) {
        List<ResolvedLine> lines = new ArrayList<>();
        for (Map.Entry<Long, Integer> e : merged.entrySet()) {
            FoodItem food = foodItemMapper.selectById(e.getKey());
            if (food == null) {
                throw new BizException("商品不存在");
            }
            if (!Integer.valueOf(FoodItemStatus.ON_SHELF.getCode()).equals(food.getStatus())) {
                throw new BizException("「" + food.getName() + "」已下架，无法下单");
            }
            if (food.getStock() == null || food.getStock() < e.getValue()) {
                int left = food.getStock() == null ? 0 : food.getStock();
                throw new BizException("「" + food.getName() + "」库存不足，当前仅剩 " + left + " 份");
            }
            BigDecimal amount = food.getPrice().multiply(BigDecimal.valueOf(e.getValue()));
            lines.add(new ResolvedLine(food, e.getValue(), amount));
        }
        return lines;
    }

    private void assertIdempotent(Long userId, String token) {
        String key = IDEM_KEY_PREFIX + userId + ":" + token.trim();
        Boolean ok = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, "1", Duration.ofMinutes(30));
        if (Boolean.FALSE.equals(ok)) {
            throw new BizException("请勿重复提交订单");
        }
    }

    private static String buildOrderNo(Long userId) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int rnd = ThreadLocalRandom.current().nextInt(100, 1000);
        return "CF" + time + String.format("%04d", userId % 10000) + rnd;
    }

    private static String blankToNull(String s) {
        return StringUtils.hasText(s) ? s.trim() : null;
    }

    private Long requireStudentUser(String message) {
        Long curId = UserContext.getUserId();
        if (curId == null) {
            throw new BizException("请先登录");
        }
        Integer roleId = UserContext.getRoleId();
        if (roleId == null || roleId != ROLE_USER) {
            throw new BizException(message);
        }
        return curId;
    }

    private static final class ResolvedLine {
        private final FoodItem food;
        private final int quantity;
        private final BigDecimal amount;

        private ResolvedLine(FoodItem food, int quantity, BigDecimal amount) {
            this.food = food;
            this.quantity = quantity;
            this.amount = amount;
        }
    }
}
