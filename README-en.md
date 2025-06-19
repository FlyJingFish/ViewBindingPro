
<h4 align="right">
  <strong>简体中文</strong> | <a href="https://github.com/FlyJingFish/ViewBindingPro/blob/master/README.md">English</a>
</h4>

<p align="center">
  <strong>
    🔥🔥🔥增强ViewBinding的使用场景
    <a>ViewBindingPro</a>
  </strong>
</p>

<p align="center">
  <a href="https://central.sonatype.com/artifact/io.github.flyjingfish/viewbindingpro-core"><img
    src="https://img.shields.io/maven-central/v/io.github.flyjingfish/viewbindingpro-core"
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

### 一、项目根目录 `build.gradle` 引入插件（必须）


- 新版本

  ```gradle
  
  plugins {
      //必须项 👇 apply 设置为 true 
      id "io.github.flyjingfish.viewbindingpro" version "1.0.5" apply true
  }
  ```

- 或者老版本

  ```gradle
    buildscript {
        dependencies {
            //必须项 👇
            classpath 'io.github.flyjingfish:viewbindingpro-plugin:1.0.5'
        }
    }
    // 👇加上这句自动为所有module设置
    apply plugin: "viewbinding.pro"
    ```


### 二、引入依赖库(必须)

```gradle
dependencies {
    //必须项 👇
    implementation 'io.github.flyjingfish:viewbindingpro-core:1.0.5'
}
```

> [!TIP]
> 如果你希望打包后的代码中不包含 `viewbindingpro-core` 库，可以改为 `compileOnly` 方式引入


### 三、使用方法

#### ViewBinding

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

#### 绑定class

- BaseActivity

```kotlin
abstract class BaseVMActivity<VB :ViewBinding,VM: ViewModel>:BaseActivity<VB>() {
    @BindClass(position = 1, insertMethodName = "void onCreate(android.os.Bundle)", callMethodName = "androidx.lifecycle.ViewModel initViewModel(java.lang.Class)",isProtected = false)
    protected lateinit var mViewModel : VM


    fun initViewModel(clazz: Class<out ViewModel>):ViewModel {
        return ViewModelProvider(this)[clazz]
    }
}
```

- BaseFragment

```kotlin
abstract class BaseVMFragment<VB :ViewBinding,VM: ViewModel>:BaseFragment<VB>() {
  @BindClass(position = 1, insertMethodName = "android.view.View onCreateView(android.view.LayoutInflater,android.view.ViewGroup,android.os.Bundle)", callMethodName = "androidx.lifecycle.ViewModel initViewModel(java.lang.Class)",isProtected = false)
  protected lateinit var mViewModel : VM

  fun initViewModel(clazz: Class<out ViewModel>):ViewModel {
    return ViewModelProvider(this)[clazz]
  }
}
```

上述两个例子都会在实现类的相应方法中，调用 `initViewModel`

#### 取消注入代码

```kotlin
@CancelBindViewBinding
@CancelBindClass
class MainActivity:BaseVMActivity<ActivityMainBinding,ExampleViewModel>() {
}
```

- CancelBindViewBinding 是取消注入 ViewBinding
- CancelBindClass 是取消注入 class

### 四、开关（非必须）

在根目录的 `gradle.properties` 中增加如下设置

```properties
#设置为false即可关闭自动注入功能
viewbindingpro.enable = true 
```

### 番外

如果你的module全部为 kotlin 代码，有可能插件不生效，目前的处理方法有两个

- 1、在根目录的 `gradle.properties` 中增加如下设置

```properties
android.defaults.buildfeatures.buildconfig=true
```

- 2、手动为不起作用的 module 增加一个 java 代码

### 最后推荐我写的另外一些库

- [OpenImage 轻松实现在应用内点击小图查看大图的动画放大效果](https://github.com/FlyJingFish/OpenImage)

- [ShapeImageView 支持显示任意图形，只有你想不到没有它做不到](https://github.com/FlyJingFish/ShapeImageView)

- [GraphicsDrawable 支持显示任意图形，但更轻量](https://github.com/FlyJingFish/GraphicsDrawable)

- [ModuleCommunication 解决模块间的通信需求，更有方便的router功能](https://github.com/FlyJingFish/ModuleCommunication)

- [FormatTextViewLib 支持部分文本设置加粗、斜体、大小、下划线、删除线，下划线支持自定义距离、颜色、线的宽度；支持添加网络或本地图片](https://github.com/FlyJingFish/FormatTextViewLib)

- [主页查看更多开源库](https://github.com/FlyJingFish)