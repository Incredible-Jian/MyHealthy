# MyHealthy 慢病健康医疗管理系统

MyHealthy 是课程实训项目，包含患者端、管理端和 Java 后端。课程演示覆盖模拟健康测量与趋势、健康档案与随访、健康报告、商城与秒杀、本地模拟支付及在线咨询。数据库初始化脚本包含课程演示数据。

## 在 Windows 上部署

准备 Git、已启动的 Docker Desktop（Linux 容器）、JDK 11（或课程使用的 JDK 18）、Maven 3.9 和 Node.js 22。使用 PowerShell，先确认 `docker compose version`、`java -version`、`mvn -version` 和 `node -v` 可用。首次构建需要联网下载镜像和依赖。

### 1. 克隆项目，启动基础服务

```powershell
git clone https://github.com/Incredible-Jian/MyHealthy.git
Set-Location MyHealthy
Copy-Item .env.example .env
notepad .env
```

将 `.env` 中的三个占位值分别替换为本机值。建议每项都用独立的随机字母数字值，可以用 `[guid]::NewGuid().ToString('N')` 生成。MySQL 业务用户和 RabbitMQ 共用 `MYSQL_PASSWORD` 配置。保存后执行：

```powershell
docker compose up -d --wait
docker compose ps
```

MySQL 首次创建数据卷时按文件名顺序导入 `database/` 下的 SQL。MySQL、Redis 和 RabbitMQ 对本机开放的端口分别是 `13316`、`16379` 和 `15673`。RabbitMQ 管理界面在 http://127.0.0.1:15674，用户名为 `myhealthy`，密码取自本机的 `MYSQL_PASSWORD`。

### 2. 构建并启动后端

在仓库根目录执行，保留终端。重新构建前先停止正在运行的后端。

```powershell
mvn -B -f backend/pom.xml clean verify
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/start-backend.ps1
```

后端使用 `druid,course` 配置，默认端口为 `18080`。启动后在另一个终端运行 `Invoke-RestMethod http://127.0.0.1:18080/captchaImage`，预期返回 `code = 200`。

### 3. 启动两个前端

从仓库根目录打开两个 PowerShell 窗口，分别执行：

```powershell
Set-Location newui
npm ci
npm run dev
```

```powershell
Set-Location admin-ui
npm ci
npm run dev
```

两个前端都使用 `npm run build:prod` 生成生产构建。如果要构建，可在各自目录的另一个终端运行。

| 入口 | 地址 | 演示账号 |
| --- | --- | --- |
| 患者端 | http://127.0.0.1:18082 | `zhangsan` |
| 管理端 | http://127.0.0.1:18081 | `healthteacher` 或 `admin` |

三个演示账号的初始密码均为 `admin123`。登录后可在患者端查看测量与趋势，在管理端查看健康档案、随访和报告。支付流程可使用本地模拟支付。演示账号和配置用于本地课程环境，部署到公网前需要另行配置认证、演示账号和访问控制。

## 可选联调

支付宝和 Coze 默认关闭。需要联调时，在仓库根目录复制配置示例，填入自己的凭据并重启后端：

```powershell
$resources = 'backend/ruoyi-admin/src/main/resources'
Copy-Item "$resources/application-secrets.local.yml.example" "$resources/application-secrets.local.yml"
notepad "$resources/application-secrets.local.yml"
```

- **支付宝沙箱**。填写沙箱网关、应用 ID、卖家 ID、应用私钥和同一应用对应的支付宝公钥，设置 `health.alipay.enabled: true`。`notify-url` 使用支付宝服务器可访问的完整 HTTPS 地址，路径为 `/health/alipay/notify`，由反向代理转发到你的后端；`return-url` 指向患者端的 `/health-payment-return`。支付状态以服务端验签和订单校验为准。
- **Coze**。填写中国站的访问令牌和六个功能对应的已发布 Bot ID，设置 `health.coze.enabled: true`。令牌需要获得对应空间和聊天接口的访问权限。项目调用 `https://api.coze.cn`。
- 保留 `.env` 与 `application-secrets.local.yml` 在本机。各部署者的数据库内容、账号凭据和外部服务配置独立。

## 文件与排查

课程回归脚本位于 `scripts/` 并使用默认端口和演示账号。运行前先创建 `docs` 目录用于保存测试结果。回归会向数据库写入测试记录，请在演示环境执行。

| 路径 | 用途 |
| --- | --- |
| `backend/` | RuoYi 后端及 MyHealthy 业务模块 |
| `newui/`、`admin-ui/` | 患者端、管理端 |
| `database/`、`mock/` | 初始化 SQL、课程模拟测量数据 |
| `compose.yml`、`scripts/` | 基础服务、启动及课程验证脚本 |

- **端口占用**。关闭占用的本机服务，或者同步调整 Compose 映射、后端 course 配置及前端代理目标。
- **数据库认证失败**。检查 `.env` 是否与首次初始化数据卷时一致。MySQL 对已经初始化的数据卷保留原账号和密码。
- **后端连接失败**。检查 `docker compose ps`，可用 `docker compose logs mysql` 查看初始化状态。从仓库根目录运行启动脚本以加载课程配置。
- **结束演示**。对 Java 和两个前端终端按 Ctrl+C，在根目录运行 `docker compose down`，数据卷会保留。
