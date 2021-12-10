# AD-HikVideoModule

海康视频SDK，由于本人只是Android开发，所以只有Android版本的实时视频预览

使用须知：由于该插件是自己项目适配的，没有做成便利性可拓展性强的版本，所以购买前请仔细阅读本文档，看是否符合自己需求



# 引入插件

    var testModule = uni.requireNativePlugin("Hik-Video-Module");
# 调用

调用插件：  （ps：1.1版本已移除此方法中的监听回调）

    testModule.gotoPreviewActivity({
        				'previewUri':this.previewUri,
    					'canControl':this.control,
    					'canRecord':this.canRecord,
    					'cameraCode':this.cameraCode,
    					'enableSound':this.enableSound
    				})
插件销毁通知回调：

    testModule.addClosePluginListener((ret)=>{
            console.log('监听到了状态变化 --- ', ret.msg) 
        })
喊话（录音）保存地址回调：(不实现这个方法就没有相应的按钮图标)

    testModule.addRecordEventListener((ret)=>{
        console.log('监听到了状态变化 --- ', ret.path) 
        })
	
# 相关代码  

    <template>
        <view class="container">
            previewUri<input v-model="previewUri" placeholder="点击编辑视频URL" >
            <br>
    		control<input v-model="control" placeholder="是否控制云台(true/false)" >
    		<br>
    		cameraCode<input v-model="cameraCode" placeholder="点击编辑相机code(重连需要)" >
    		<br>
            <button type="default" @click="test">提交</button>
    		
            <view>{{previewUri}}</view>
    		<view>{{control}}</view>
    		<view>{{cameraCode}}</view>
            <!-- <view>{{ result }}</view> -->
        </view>
    </template>
    
    <script>
        var testModule = uni.requireNativePlugin("Hik-Video-Module");
        export default {
            data() {
                return {
                    previewUri:"",
                    control:true,
    				canRecord:true,
    				enableSound:false,
    				cameraCode:"",
                    result:"return msg",
                }
            },
            
        onLoad() {
			this.addRecordListener();
			this.addClosePluginListener();
		},
		onUnload() {
			console.log('ddd');
		},
        methods: {
			test(){
				testModule.gotoPreviewActivity({
					'previewUri':this.previewUri,
					'canControl':this.control,
					'canRecord':this.canRecord,
					'cameraCode':this.cameraCode,
					'enableSound':true
				})
			},
			addRecordListener(){
				testModule.addRecordEventListener((ret)=>{
					console.log('监听到了状态变化 --- ', ret.path) 
				})
			},
			addClosePluginListener(){
				testModule.addClosePluginListener((ret)=>{
					console.log('监听到了状态变化 --- ', ret.msg) 
				})
			}
			
        }
        }
    </script>


    				
# API  

### gotoPreviewActivity() :  参数 -> （*） 为必传

    * previewUri：前端直接传入URL 进入就直接播放，不给就需要本地获取（测试时建议填写）。播放失败就进行三次重连，
    * cameraCode： 重连需要的相机参数,根据相机参数去获取的视频流地址。
      canControl： 是否显示控制按钮，canControl == false ,录音按钮也不会显示
      canRecord ： 是否显示录音按钮
      enableSound: 是否开始视频声音，false关闭 true开启 默认是关闭的 

### addRecordEventListener()

    添加录音监听回调事件,返回值为语音存储位置。
    
### addClosePluginListener() 

    插件销毁监听回调


# 注意事项

## 1 在manifest中选择插件Hik-Video-Module,然后配置相应参数  

#### cameraControlUrl 
控制摄像头进行转动等相应操作 （注意：配置信息末端的 ? 已经在1.2版本后移除）


~~cameraControlUrl ：http:/xxxxxx/ipcControlCenter/ptz/controlling?~~

    cameraControlUrl ：http:/xxxxxx/ipcControlCenter/ptz/controlling
其他参数如下，都是在插件里面已经配置好的

        HttpTools.okHttpGet(cameraControlUrl + "?action=" + action +
                "&code=" + cameraCode + "&command=" + command +
                "&speed=" + cameraSpeed);

#### ControlUrl
获取视频流URL接口
    ControlUrl: http://xxxxxx/ipcControlCenter/ptz/previewURLs?protocol=rtsp //本项目最开始传入的URL地址会在几分钟后失效，如果用户在此期间切出APP后在进入就需要重连（默认3次），重新获取URL。

    HttpTools.okHttpGet(ControlUrl + "&code=" + cameraCode) 
    
这里注意，有些视频流是区分UDP、TCP的，这里并没有进行传参，统一交给后端设置

由于每个项目公司间接口规则不一致，可能需要修改Android源码，但是如果是遵循**海康相机控制接口规则**（详情可参考：https://open.hikvision.com/ ），那么就配置后就能使用插件。

# 常见问题汇总
#### 1 抓图或录像无效
请检查是否给APP读写权限
#### 2 录音无效
请检查是否给APP读写权限
#### 3 录音无法在喇叭上播放
录制音频的格式为MPEG_4 ，编码 AAC，在手机上是能正常播放的，可能会遇到某些要求特定的采样率、 比特率的喇叭导致无法播放


# 后记  
 
讲道理，不配置其他参数的情况下只要previewUri正确就能获取到视频画面。  


当然也欢迎各位在使用插件过程中有好的建议或者发现bug及时反馈给作者。再次感谢！
