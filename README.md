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

- New version (under review, not available yet)

    ```gradle
    
    plugins {
        //Required item 👇 apply Set to true to automatically "pre-configure" debugMode for all modules, false, follow the second method of step 5 below
        id "io.github.FlyJingFish.ViewBindingPro" version "1.0.0" apply true
    }
    ```

- Or old version
    
    ```gradle
    buildscript {
        dependencies {
            //Required item 👇
            classpath 'io.github.FlyJingFish.ViewBindingPro:viewbindingpro-plugin:1.0.0'
        }
    }
    // 👇 Add this sentence to automatically "pre-configure" debugMode for all modules, if not, follow the second method of step 5 below
    apply plugin: "viewbinding.pro"
    ```

### 2. Introduce dependent libraries (required)

```gradle

dependencies {
    //Required 👇
    implementation 'io.github.FlyJingFish.ViewBindingPro:viewbindingpro-core:1.0.0'
}
```

### 3. Usage

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

### 4.Switch(optional)

```properties
#Set to false to turn off the automatic injection function
viewbindingpro.enable = true
```

### Finally, I recommend some other libraries I wrote

- [OpenImage easily realizes the animated zoom effect of clicking on the small image in the application to view the large image](https://github.com/FlyJingFish/OpenImage)

- [ShapeImageView supports the display of any graphics, it can do anything you can't think of](https://github.com/FlyJingFish/ShapeImageView)

- [GraphicsDrawable supports the display of any graphics, but it is lighter](https://github.com/FlyJingFish/GraphicsDrawable)

- [ModuleCommunication solves the communication needs between modules, and has a more convenient router function](https://github.com/FlyJingFish/ModuleCommunication)

- [FormatTextViewLib Supports bold, italic, size, underline, and strikethrough for some texts. Underline supports custom distance, color, and line width; supports adding network or local images](https://github.com/FlyJingFish/FormatTextViewLib)

- [Homepage View more open source libraries](https://github.com/FlyJingFish)