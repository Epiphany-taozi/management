# 物业管理数据库课程设计

本项目是一个基于 **JavaFX + MySQL** 的物业管理课程设计示例，重点演示业主、员工、资产、投诉、收费与车位使用等业务数据的建模与展示，并提供配套的数据库脚本与基础界面。

## 功能概览

- **业主管理**：业主信息维护、房屋信息展示
- **员工管理**：员工信息维护与展示
- **公共资产管理**：资产状态与位置管理
- **投诉管理**：投诉记录与处理状态展示
- **收费查询**：收费明细与汇总展示
- **车位使用**：车位分配、使用记录与统计
- **报修模块（数据结构）**：报修主表与处理日志示例

> 注：当前项目以数据库结构与 JavaFX 展示为主，部分业务为数据展示示例。

## 技术栈

- Java 21
- JavaFX 21
- MySQL 8+（或兼容版本）
- Maven

## 目录结构

```
.
├── pom.xml
├── README.md
└── src/main
    ├── java/com/ryan/property
    │   ├── MainApp.java
    │   ├── controller
    │   ├── dao
    │   ├── db
    │   ├── fees
    │   ├── model
    │   ├── service
    │   └── ui
    └── resources
        ├── application.properties
        ├── schema.sql
        ├── parking_schema.sql
        ├── repairs.sql
        ├── fxml
        └── css
```

## 数据库脚本

按需执行以下脚本创建表结构：

- `schema.sql`：业主、员工、资产、投诉、收费等核心表
- `parking_schema.sql`：车位分配与使用记录表
- `repairs.sql`：报修主表与处理日志表

建议先创建数据库：

```sql
CREATE DATABASE property_db DEFAULT CHARACTER SET utf8mb4;
```

然后依次执行脚本：

```bash
mysql -u root -p property_db < src/main/resources/schema.sql
mysql -u root -p property_db < src/main/resources/parking_schema.sql
mysql -u root -p property_db < src/main/resources/repairs.sql
```

## 配置说明

数据库连接信息在 `src/main/resources/application.properties` 中配置：

```
db.url=jdbc:mysql://localhost:3306/property_db?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8&allowPublicKeyRetrieval=true
db.user=root
db.password=123456
```

请根据本地数据库账号与密码进行修改。

## 运行方式

```bash
mvn clean javafx:run
```

运行后将启动 JavaFX 界面。

## 常见问题

- **数据库连接失败**：请确认数据库已创建且脚本已执行，账号密码配置正确。
- **JavaFX 启动失败**：请使用 JDK 21，并确保 Maven 已正确安装。

## 许可证

仅用于教学与课程设计示例。
