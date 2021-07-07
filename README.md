# AndroidCache

## 一个带过时自动清理的本地缓存库
通过数据库的记录和WorkManager的管理，使得借助该工具缓存到手机本地软件cache内的文件能及时清理  
设计的原因：为了避免大量而过时的数据不停低生成到cache中，并且不及时删除导致应用占用空间越来越大。采取通过数据库，绑定文件的Md5,携带压缩的规则，对文件路径进行管理，相同文件的MD5和参数
，输出缓冲目录下相同且存在的文件，避免重复写入大量相同数据。  
举例：  
Bitmap的 MD5计算如此：  
```
val md5Name = Util.value2MD5(ByteBuffer.allocate(scaleBitmap.byteCount).let {
                scaleBitmap.copyPixelsToBuffer(it)
                it.array()
            }) + "[${minWidth}X${minHeight}X${format.name}]"
```

利用这个作为表的主键，进行限制，当本地文件被人为删除后，改库会在再次调用时候，进行记录的覆盖。  
通过被调用的次数去处理过时文件，默认为创建后1小时候后如未被再次使用则本地cache文件会被删除，下一次相同内容将重新进行写入操作。
