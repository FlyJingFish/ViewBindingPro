<p align="center">
  <strong>
    🔥🔥🔥增强ViewBinding的使用场景
    <a>ViewBindingPro</a>
  </strong>
</p>

<p align="center">
  <a href="https://central.sonatype.com/search?q=io.github.FlyJingFish.ViewBindingPro"><img
    src="https://img.shields.io/maven-central/v/io.github.FlyJingFish.ViewBindingPro/viewbindingpro-core"
    alt="Build"
  /></a>
  <a href="https://github.com/FlyJingFish/ViewBindingPro/stargazers"><img
    src="https://img.shields.io/github/stars/FlyJingFish/ViewBindingPro.svg?style=flat"
    alt="Downloads"
  /></a>
  <a href="https://github.com/FlyJingFish/ViewBindingPro/network/members"><img
    src="https://img.shields.io/github/forks/FlyJingFish/ViewBindingPro.svg?style=flat"
    alt="Python Package Index"
  /></a>
  <a href="https://github.com/FlyJingFish/ViewBindingPro/issues"><img
    src="https://img.shields.io/github/issues/FlyJingFish/ViewBindingPro.svg?style=flat"
    alt="Docker Pulls"
  /></a>
  <a href="https://github.com/FlyJingFish/ViewBindingPro/blob/master/LICENSE"><img
    src="https://img.shields.io/github/license/FlyJingFish/ViewBindingPro.svg?style=flat"
    alt="Sponsors"
  /></a>
</p>



# 简述

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;使用这个框架之后，您在 BaseActivity 或 BaseFragment 等基类配置一个注解就可以为 子类自动注入加载代码，无需使用反射



## 使用步骤

**在开始之前可以给项目一个Star吗？非常感谢，你的支持是我唯一的动力。欢迎Star和Issues!**

### 一、引入插件（必须）


- 新版本（审核中，暂不可用）

  ```gradle
  
  plugins {
      //必须项 👇 apply 设置为 true 自动为所有module“预”配置debugMode，false则按下边步骤五的方式二
      id "io.github.FlyJingFish.ViewBindingPro" version "1.0.0" apply true
  }
  ```
  
- 或者老版本

  ```gradle
    buildscript {
        dependencies {
            //必须项 👇
            classpath 'io.github.FlyJingFish.ViewBindingPro:viewbindingpro-plugin:1.0.0'
        }
    }
    // 👇加上这句自动为所有module“预”配置debugMode，不加则按下边步骤五的方式二
    apply plugin: "viewbinding.pro"
    ```


### 二、引入依赖库(必须)

```gradle
plugins {
    //非必须项 👇，如果需要自定义切面，并且使用 android-aop-ksp 这个库的话需要配置 
    id 'com.google.devtools.ksp'
}

dependencies {
    //必须项 👇
    implementation 'io.github.FlyJingFish.ViewBindingPro:viewbindingpro-core:1.0.0'
}
```

### 三、使用方法

- BaseActivity 

```kotlin
abstract class BaseActivity<VB :ViewBinding>:AppCompatActivity() {
    @BindViewBinding(position = 0, methodName = "void onCreate(android.os.Bundle)", isProtected = true, bindingType = BingType.INFLATE)
    protected lateinit var binding :VB


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

}
```

- BaseFragment

```kotlin
abstract class BaseFragment<VB : ViewBinding>: Fragment() {
    @BindViewBinding(position = 0, methodName = "android.view.View onCreateView(android.view.LayoutInflater,android.view.ViewGroup,android.os.Bundle)",  isProtected = false,bindingType = BingType.INFLATE_FALSE)
    protected lateinit var binding :VB
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }
}
```

### 四、开关（非必须）

```properties
#设置为false即可关闭自动注入功能
viewbindingpro.enable = true 
```



### 最后推荐我写的另外一些库

- [OpenImage 轻松实现在应用内点击小图查看大图的动画放大效果](https://github.com/FlyJingFish/OpenImage)

- [ShapeImageView 支持显示任意图形，只有你想不到没有它做不到](https://github.com/FlyJingFish/ShapeImageView)

- [GraphicsDrawable 支持显示任意图形，但更轻量](https://github.com/FlyJingFish/GraphicsDrawable)

- [ModuleCommunication 解决模块间的通信需求，更有方便的router功能](https://github.com/FlyJingFish/ModuleCommunication)

- [FormatTextViewLib 支持部分文本设置加粗、斜体、大小、下划线、删除线，下划线支持自定义距离、颜色、线的宽度；支持添加网络或本地图片](https://github.com/FlyJingFish/FormatTextViewLib)

- [主页查看更多开源库](https://github.com/FlyJingFish)

