# RSA 密钥对

此目录存放 JWT 签名所需的 RSA 密钥对。

## 生成密钥

```bash
# 生成 2048 位 RSA 私钥
openssl genpkey -algorithm RSA -out private.pem -pkeyopt rsa_keygen_bits:2048

# 导出对应的公钥
openssl rsa -pubout -in private.pem -out public.pem
```

## 注意事项

- `private.pem` 已被 `.gitignore` 排除，**不会提交到 Git**
- `public.pem` 可以安全提交（公钥本身不涉密）
- 生产环境**必须**使用独立生成的密钥对，禁止复用开发密钥
- 建议生产环境通过外部挂载或环境变量指定密钥路径
