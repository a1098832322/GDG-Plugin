# 郭德纲相声插件
![Build日期](https://img.shields.io/badge/Build-2020--12--10-green) 
![版本](https://img.shields.io/badge/Version-1.2-brightgreen)
![license](https://img.shields.io/badge/License-Apache%202.0-blue) 

代码写累了吗？不如摸一会儿鱼，听段儿相声吧~~~ 

这是一个适用于IDEA(或其他jetbrains全家桶)的郭德纲相声插件

### ref
1、音频核心依赖包 [点击查看](https://github.com/a1098832322/AudioCore)

### Change Log
 #### v1.3.1 Build 2022-01-25
```
更新了一个新版IDEA插件开发手册中被标记为已过期的API
```
 #### v1.3 Build 2021-01-15
```
修复若干bug:
  1.修复停止按钮点击时会自动加载下一曲的bug
  2.新增无法播放的VIP曲目提示
```
 #### v1.2 Build 2020-12-10
 ``` 
 增加若干大家可能会用得到的功能。  
  1.新增按列表顺序播放功能
  2.新增按艺术家(自定义关键字)搜索曲目功能[部分搜索结果播放可能会有问题导致播放不出来。预计下个版本解决]  
    输入艺术家名字后，点击"Go"按钮进行搜索。
  3.自动清理M4A格式的音频缓存文件，节省磁盘空间
      
  ``` 
 #### v1.1 Build 2020-12-8
 ``` 
 增加进度条功能。  
 本来是觉得安静听相声就好了的，结果后来发现不能自由调整播放进度也是个麻烦。So就加了个进度条。
  ``` 
 #### v1.0 Build 2020-11-9
 ``` 
 初版 支持自定义缓存文件路径，支持跨平台。  
 使用喜马拉雅的API，插件会先从目标网址获取音频，因为源格式为M4A，因此在下载完成后还会自动进行转码，待转码成MP3后才会进行播放。初次下载请耐心等待！
  ```  
