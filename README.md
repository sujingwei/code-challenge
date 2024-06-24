# code-challenge


## 一、引言
### 1. 背景
Imaging a simplified scenario where you are asked to implement 2 microservices called “ping” and “pong” respectively and integrates with each other as illustrated below...
### 2. 编写目的
当前文档编写目的主要是从业务方向描述当前项目的设计架构，如当前项目有多少个模块，模块与模块之间的关系。如何安装并部署项目，如何运行测试脚本进行测试操作。

## 二、项目介绍
项目名称为：code-challenge，包含3个子模块，分别为：

| # | 模块名称       | 模块描述                                              |
|---|:-----------|:--------------------------------------------------|
| 1 | pong       | pong服务，提供回复接口                                     |
| 2 | ping       | ping服务，提供请求接口，可以访问pong服务                          |
| 3 | assisttest | 当前ping使用集群方式部署时，项目提供的一个统一调用调试应用，可以快速访问ping集群的多个服务 |

## 三. Building & Running
### 1. 项目构建
```shell
mvn clean package -Dmaven.test.skip=true
```
### 2. 项目运行
#### 2.1 运行pong服务
pong默认的端口为:8080
运行脚本如下 ：
```shell
java -jar .pong-1.0.0.jar --server.port=8080
```
#### 2.2 运行ping服务
运行脚本如下 ：
```shell
java -jar .ping-1.0.0.jar --server.port=8081
java -jar .ping-1.0.0.jar --server.port=8082
java -jar .ping-1.0.0.jar --server.port=8083
```
如果pong服务的地址（包含端口）发生改变， 可以通过`--pong.server.address="http://localhost:8080"`参数指定pong服务的地下。

#### 2.3 运行assisttest应用
assisttest-xxx.jar是为了给ping集群服务提供一个快速测试的脚本，它的主要业务逻辑是给ping集群服务发出请求，它的主要配置参数有以下3个：
1. `--send.requests`，每秒发送请求的数量；默认为：6个
2. `--send.seconds`，持续发出请求时间，单位为秒；默认为：10秒
3. `--ping.addresses`，ping服务集群地址，多个地址使用","分隔；默认值：`http://127.0.0.1:8081,http://127.0.0.1:8082,http://127.0.0.1:8083`

运行脚本如下 ：
```shell
java -jar assisttest-1.0.0.jar --send.requests=10 --send.seconds=8 --ping.addresses="http://127.0.0.1:8081,http://127.0.0.1:8082,http://127.0.0.1:8083"
```
脚本运行执行以下操作：
> 平均在1秒内向ping服务集群发出10个请求，持续发送时间为8秒