### 流程

1. 通过ADB截屏；
2. 通过ADB将截屏保存到电脑；
3. 识别玩家位置；
4. 识别目标方块位置；
5. 识别目标方块中心圆点的位置；
6. 如果第5步成功，则取第5步的中心点为下一步的位置；否则，取第4步的中心点为下一步的位置；
7. 计算玩家位置与下一步的位置，乘以一定的系数，得到长按的时间；
8. 通过ADB，触发长按；
	
## 三. 运行教程

1. 准备Java运行与编译环境，使用Java8以上，IDE推荐使用Intellij；
2. 安装Android SDK；
3. 使用 git工具clone项目，地址为it@github.com:Huang-Jacky/wechat_jump.git
4. 使用IDE Android Studio import该项目；
5. 准备好一部已经打开开发者模式的Android手机连接到电脑
6. 打开开发者选项，找到“USB调试（安全设置）允许通过USB调试修改权限或者模拟点击”
7. 打开微信，打开跳一跳游戏，并点击开始；
