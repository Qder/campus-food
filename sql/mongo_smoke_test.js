// MongoDB 冒烟测试脚本：验证集合、索引与多类型消息读写
db.chat_message.deleteMany({ sessionId: 999 });

db.chat_message.insertMany([
  { sessionId: 999, fromUserId: 1, msgType: "TEXT",  content: "今晚吃什么？", createdAt: new Date("2026-10-24T21:00:00Z") },
  { sessionId: 999, fromUserId: 2, msgType: "TEXT",  content: "去二食堂吧",   createdAt: new Date("2026-10-24T21:01:00Z") },
  { sessionId: 999, fromUserId: 1, msgType: "IMAGE", content: "https://cdn.example.com/food.jpg", createdAt: new Date("2026-10-24T21:02:00Z") },
  { sessionId: 999, fromUserId: 2, msgType: "EMOJI", content: "😋", createdAt: new Date("2026-10-24T21:03:00Z") }
]);

print("inserted=" + db.chat_message.countDocuments({ sessionId: 999 }));
print("--- 按 createdAt 倒序（对应 findBySessionIdOrderByCreatedAtDesc）---");
db.chat_message.find({ sessionId: 999 }).sort({ createdAt: -1 }).forEach(d =>
  print(d.msgType + " | uid=" + d.fromUserId + " | " + d.content));

print("--- 索引 ---");
db.chat_message.getIndexes().forEach(i => print(i.name + " -> " + JSON.stringify(i.key)));

print("--- 查询计划（应命中 idx_session_time）---");
var plan = db.chat_message.find({ sessionId: 999 }).sort({ createdAt: -1 }).explain("executionStats");
print("stage=" + plan.queryPlanner.winningPlan.stage);
print("indexUsed=" + JSON.stringify(plan.queryPlanner.winningPlan.inputStage.keyPattern));
print("docsExamined=" + plan.executionStats.totalDocsExamined);
print("nReturned=" + plan.executionStats.nReturned);

db.chat_message.deleteMany({ sessionId: 999 });
print("cleaned, remaining=" + db.chat_message.countDocuments({}));
