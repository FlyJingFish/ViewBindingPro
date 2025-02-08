<h4 align="right">
  <strong>English</strong> | <a href="https://github.com/FlyJingFish/ViewBindingPro/blob/master/README.md">简体中文</a>
</h4>

<p align="center"> 
    <strong> 🔥🔥🔥Enhance the usage scenarios of ViewBinding 
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

# Brief description

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;After using this framework, you can automatically
inject loading code into subclasses by configuring an annotation in the base class such as
BaseActivity or BaseFragment, without using reflection

## Usage steps

**Can you give the project a Star before starting? Thank you very much, your support is my only
motivation. Stars and Issues are welcome!**

### 1. Introduce plugins (required)

- New version 

    ```gradle
    
    plugins {
        //Required item 👇 apply Set to true to automatically "pre-configure" debugMode for all modules, false, follow the second method of step 5 below
        id "io.github.FlyJingFish.ViewBindingPro" version "1.0.2" apply true
    }
    ```

- Or old version

    ```gradle
    buildscript {
        dependencies {
            //Required item 👇
            classpath 'io.github.FlyJingFish.ViewBindingPro:viewbindingpro-plugin:1.0.2'
        }
    }
    // 👇 Add this sentence to automatically "pre-configure" debugMode for all modules, if not, follow the second method of step 5 below
    apply plugin: "viewbinding.pro"
    ```

### 2. Introduce dependent libraries (required)

```gradle

dependencies {
    //Required 👇
    implementation 'io.github.FlyJingFish.ViewBindingPro:viewbindingpro-core:1.0.2'
}
```

> [!TIP]
> If you want the packaged code to not include the `viewbindingpro-core` library, you can change it to `compileOnly` to import it

### 3. Usage

#### ViewBinding

- BaseActivity

```kotlin
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {
    @BindViewBinding(
        position = 0,
        methodName = "void onCreate(android.os.Bundle)",
        isProtected = true,
        bindingType = BingType.INFLATE
    )
    protected lateinit var binding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) setContentView (binding.root)
    }
} 
``` 

- BaseFragment

```kotlin 
abstract class BaseFragment<VB : ViewBinding> : Fragment() {
    @BindViewBinding(
        position = 0,
        methodName = "android.view.View onCreateView(android.view.LayoutInflater,android.view.ViewGroup,android.os.Bundle)",
        isProtected = false,
        bindingType = BingType.INFLATE_FALSE
    )
    protected lateinit var binding: VB
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }
}

```

#### Binding class 
- BaseActivity
```kotlin 
abstract class BaseVMActivity<VB : ViewBinding, VM : ViewModel> : BaseActivity<VB>() {
  @BindClass(
    position = 1,
    insertMethodName = "void onCreate(android.os.Bundle)",
    callMethodName = "androidx.lifecycle.ViewModel initViewModel(java.lang.Class)",
    isProtected = true
  )
  protected lateinit var mViewModel: VM 
  
  fun initViewModel(clazz: Class<out ViewModel>): ViewModel {
    return ViewModelProvider(this)[clazz]
  }
} 
``` 
- BaseFragment 
```kotlin
abstract class BaseVMFragment<VB : ViewBinding, VM : ViewModel> : BaseFragment<VB>() {
  @BindClass(
    position = 1,
    insertMethodName = "android.view.View onCreateView(android.view.LayoutInflater,android.view.ViewGroup,android.os.Bundle)",
    callMethodName = "androidx.lifecycle.ViewModel initViewModel(java.lang.Class)",
    isProtected = false
  )
  protected lateinit var mViewModel: VM

  fun initViewModel(clazz: Class<out ViewModel>): ViewModel {
    return ViewModelProvider(this)[clazz]
  }
}
```

Both examples above will call `initViewModel` in the corresponding methods of the implementation class

#### Cancel injection code

```kotlin
@CancelBindViewBinding
@CancelBindClass
class MainActivity:BaseVMActivity<ActivityMainBinding,ExampleViewModel>() {
}
```

- CancelBindViewBinding cancels ViewBinding injection
- CancelBindClass cancels class injection

### 4.Switch(optional)

Add the following settings to the root directory's `gradle.properties`

```properties
#Set to false to turn off the automatic injection function
viewbindingpro.enable = true
```

### Extra

If your module is all kotlin code, the plugin may not work. There are currently two ways to deal with it

- 1. Add the following settings to the root directory's `gradle.properties`

```properties
android.defaults.buildfeatures.buildconfig=true
```

- 2. Manually add a java code to the module that does not work

### Finally, I recommend some other libraries I wrote

- [OpenImage easily realizes the animated zoom effect of clicking on the small image in the application to view the large image](https://github.com/FlyJingFish/OpenImage)

- [ShapeImageView supports the display of any graphics, it can do anything you can't think of](https://github.com/FlyJingFish/ShapeImageView)

- [GraphicsDrawable supports the display of any graphics, but it is lighter](https://github.com/FlyJingFish/GraphicsDrawable)

- [ModuleCommunication solves the communication needs between modules, and has a more convenient router function](https://github.com/FlyJingFish/ModuleCommunication)

- [FormatTextViewLib Supports bold, italic, size, underline, and strikethrough for some texts. Underline supports custom distance, color, and line width; supports adding network or local images](https://github.com/FlyJingFish/FormatTextViewLib)

- [Homepage View more open source libraries](https://github.com/FlyJingFish)

