[中文](README.md) |
[ENGLISH](README_EN.md)

## 简介

Mybatis-Sql-Viewer是一款增强idea对mybatis支持的插件，主要功能如下：<br/>
<ul>
<li>快速从代码跳转到mapper及从mapper返回代码</li>
<li>mapper文件中的xml语句/方法转换成sql语句，支持参数mock、数据源配置、sql执行</li>
</ul>

## 安装

`IntelliJ IDEA` > `Preferences` > `Plugins` > `Marketplace` > `Search for "mybatis sql view"` > `Install Plugin` > `Restart IntelliJ IDEA`

## 使用

![](https://linyimin-picture.oss-cn-hangzhou.aliyuncs.com/how-to-use.gif)

## 注意

由于使用了mybatis包将xml转成sql语句，会对整个项目进行编译，第一次使用时，加载会有一定耗时，请耐心等待。