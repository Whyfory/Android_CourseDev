# 移动应用开发课程实验

在 Android Studio 中打开仓库根目录 `Dev`，等待 Gradle 同步。启动模拟器后，在运行配置中选择要查看的实验模块，再点 Run。每个模块可以单独安装。

| 模块 | 实验内容 |
| --- | --- |
| `lab01` | Android 开发基础。启动应用并确认显示 `Hello, Android!`。 |

也可以在根目录打开 PowerShell，用 Gradle Wrapper 构建：

```powershell
.\gradlew.bat :lab01:assembleDebug
```

学习入口是每个模块的 `src/main`。其中 `AndroidManifest.xml` 声明应用入口，`java/` 存放界面逻辑，`res/` 存放布局、文字和图片资源。
