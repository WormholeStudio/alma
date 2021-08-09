export default [
  // 地址和公钥换待加密串
  'v1/auth/nonce|post',
  // 拿加密串换token
  'v1/auth/token|post',
  // 项目列表
  'v1/project/list|post',
  // 我的空投列表
  'v1/airdrop/list|post',
  // 创建空投
  'v1/airdrop/create|post',
  // 删除空投
  'v1/airdrop/delete|post',
  // 领取空投
  'v1/airdrop/claim|post',
  // 发布空投
  'v1/airdrop/send|post',
  // 发放空投,就是给币了
  'v1/airdrop/publish|post',
  // 提交txn
  'v1/airdrop/commit|post',
];
