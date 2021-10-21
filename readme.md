### 1. 背景

玩过王者荣耀的同学肯定碰到过这样的场景：一段时间没打开 APP，打开的时候会提醒你下载安装升级资源包，一个完整的资源包可能超过 1 个 G ，但是升级资源包可能只有百来 M 大小；用 Android 手机的同学下载一个微信或者淘宝，完整的包大小有一两百 M 大小，但是手机应用市场提醒你更新时，会提醒你只需要下载几十 M 就可以了。这些场景都使用了差分算法，以 Android APP 为例，一个完整的 apk 包大小可能有 100 多 M 大小，每次版本升级时，如果让用户下载 100 多 M 大小的文件，无疑用户体验是很差的，文件越大越耗流量，下载成功率也越低，用户等待的时间也越长，但是如果用差分算法比较新旧两个 apk 包文件，计算出一个大小小很多的差分包来，用户更新时下载的是差分包，然后本地将原包与差分包合并成新包，对用户体验来说无疑是巨大的提升。

差分算法有很多，这里介绍一种比较常用的差分算法 `bsdiff`。

### 2. 安装步骤

#### 2.1. 下载安装编译 bsdiff

首先下载 bsdiff 库，其下载地址为：[bsdiff](https://github.com/mendsley/bsdiff/releases/tag/v4.3)，其次 bsdiff 算法里用到了 bzlib 这个压缩库，下载地址为：[bzip2](https://sourceforge.net/projects/bzip2/)

将这 2 个库解压之后，将 bzip2 文件夹和 bsdiff.c 文件放在同一目录，最终目录结构如下图所示：
![](https://upload-images.jianshu.io/upload_images/5955727-3db1ae7229ccd1c1.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

在 bsdiff.c 里添加如下引用：
```
#include "bzip2/bzlib.c"
#include "bzip2/crctable.c"
#include "bzip2/compress.c"
#include "bzip2/decompress.c"
#include "bzip2/randtable.c"
#include "bzip2/blocksort.c"
#include "bzip2/huffman.c"
```
打开命令行进入到 bsdiff 所在目录，执行 `gcc bsdiff.c` 命令编译，会生成一个 `a.out` 文件，然后就可以运行生成差分包了。
![](https://upload-images.jianshu.io/upload_images/5955727-4c523b05a6b5790e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

#### 2.2.  生成差分包
我们看下 bsdiff.c 的 main 方法：
```
int main(int argc,char *argv[])
{
        int fd;
        u_char *old,*new;
        off_t oldsize,newsize;
        off_t *I,*V;
        off_t scan,pos,len;
        off_t lastscan,lastpos,lastoffset;
        off_t oldscore,scsc;
        off_t s,Sf,lenf,Sb,lenb;
        off_t overlap,Ss,lens;
        off_t i;
        off_t dblen,eblen;
        u_char *db,*eb;
        u_char buf[8];
        u_char header[32];
        FILE * pf;
        BZFILE * pfbz2;
        int bz2err;

        if(argc!=4) errx(1,"usage: %s oldfile newfile patchfile\n",argv[0]);

        /* Allocate oldsize+1 bytes instead of oldsize bytes to ensure
                that we never try to malloc(0) and get a NULL pointer */
        if(((fd=open(argv[1],O_RDONLY,0))<0) ||
                ((oldsize=lseek(fd,0,SEEK_END))==-1) ||
                ((old=malloc(oldsize+1))==NULL) ||
                (lseek(fd,0,SEEK_SET)!=0) ||
                (read(fd,old,oldsize)!=oldsize) ||
                (close(fd)==-1)) err(1,"%s",argv[1]);

```

可以看到代码中有注释 `usage: %s oldfile newfile patchfile`，也就是 main 方法总共需要 3 个参数：oldFile(旧文件)、newFile(新文件)、patchfile(待生成的差分文件路径)。

以为自己电脑上的配置为例：
```
//注意 a.out 一定为全路径，old.bundle、new.bundle 文件都都放在同个文件夹下
/Users/hjy/Desktop/bsdiff/a.out old.bundle new.bundle patch.bundle
```
执行完成后会生成差分文件 patch.bundle，如下图所示：

![](https://upload-images.jianshu.io/upload_images/5955727-55e30651300badf3.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

说明一下，上面例子中我使用了自己的一个 react-native 项目，  `old.bundle` 文件是将 rn 工程单独打包出来的 js bundle 文件，然后再修改几行 js 代码，重新打包出来后得到的是 `new.bundle` 文件，`patch.bundle` 文件是新旧两个 js bundle 的差分包。如果我们在进行版本更新时，理论上我只需要下载 patch.bundle 文件，然后再将其与 old.bundle 合并生成 new.bundle 即可，这样就可以少下载很多东西。

在我这个例子当中 old.bundle、new.bundle 文件大小都差不多，均为 19 M 多，但我只是修改了几行代码，所以生成的差分包及其小，只有 200 多个字节的大小。

#### 2.3. 合并旧包和差分包
合并需要使用 bspatch，规则如下：
```
bspatch oldfile newfile patchfile
```
同样通过命令行进入 bsdiff 目录，将前面生成的差分包与老包合并：
```
bspatch old.bundle merge.bundle patch.bundle
```
merge.bundle 就是我们合成的新包：
![](https://upload-images.jianshu.io/upload_images/5955727-38bce5f6d50e7753.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

我们分别计算下 `new.bundle` 与 `merge.bundle` 文件的 md5 值，如果两者相同则说明我们合并是 OK 的。
```
hjydeMacBook-Pro:bsdiff hjy$ md5 new.bundle merge.bundle 
MD5 (new.bundle) = 940d3ec68c041dcd16b0740890022cf2
MD5 (merge.bundle) = 940d3ec68c041dcd16b0740890022cf2
```
上面的结果验证了我们生成差分包以及合并差分包都是正确的。

### 3. 在 Android 中使用 bsdiff
bsdiff 是采用 c 来编写，在 Android 中肯定是不能直接拿来使用的，那就需要将 bsdiff 编译成静态 so 库，然后通过 JNI 的方式来调用。

#### 3.1. 创建 jni 目录
为了方便可以新建一个 Android 工程，在 `app/src/main` 目录下新建 `jni` 文件夹，将前面的 bsdiff 源文件拷贝到 jni 目录下。

#### 3.2. 创建 java native 方法以及 .h 头文件
创建一个 Java 类 FileDiffer，在其中声明 native 方法，如下所示：
```
package com.hjy.bsdiff;
public class FileDiffer {
    public static native int fileDiff(String oldFile, String newFile, String patchFile);
    public static native int fileCombine(String oldFile, String newFile, String patchFile);
}
```
然后使用 `javah` 命令生成 .h 头文件，在 `app/src/main/java` 目录下执行命令(注意自己实际的包名，我这里为com.hjy.bsdiff)：
```
javah com.hjy.bsdiff.FileDiffer
```
执行完之后，会生成一个 `com_hjy_bsdiff_FileDiffer.h` 头文件，将该头文件拷贝到 jni 目录下。

#### 3.3. 修改 bsdiff.c、bspatch.c 
为了能让 JNI 里的方法能够调用，需要将 main 方法改造成普通的方法，然后在我们定义的 jni 方法中组装好参数之后调用。

```
#if 0
__FBSDID("$FreeBSD: src/usr.bin/bsdiff/bsdiff/bsdiff.c,v 1.1 2005/08/06 01:59:05 cperciva Exp $");
#endif

//引入我们自己生成的头文件
#include <com_hjy_bsdiff_FileDiffer.h>

#include <sys/types.h>

#include "bzip2/bzlib.c"
#include "bzip2/crctable.c"
#include "bzip2/compress.c"
#include "bzip2/decompress.c"
#include "bzip2/randtable.c"
#include "bzip2/blocksort.c"
#include "bzip2/huffman.c"

//注意删除源文件的这里
//#include <bzlib.h>

#include <err.h>
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

//引入 jni 库
#include <jni.h>

...
省略部分
...

//原来 main 方法修改
int fileDiff(int argc,char *argv[])
{
	int fd;
	u_char *old,*new;
	off_t oldsize,newsize;
	off_t *I,*V;
	off_t scan,pos,len;
	off_t lastscan,lastpos,lastoffset;
	off_t oldscore,scsc;
	off_t s,Sf,lenf,Sb,lenb;
	off_t overlap,Ss,lens;
	off_t i;
	off_t dblen,eblen;
	u_char *db,*eb;
	u_char buf[8];
	u_char header[32];
	FILE * pf;
	BZFILE * pfbz2;
	int bz2err;

	if(argc!=4) errx(1,"usage: %s oldfile newfile patchfile\n",argv[0]);
  ...
  省略部分
  ...
	return 0;
}

JNIEXPORT jint JNICALL Java_com_hjy_bsdiff_FileDiffer_fileDiff
  (JNIEnv *env, jclass clazz, jstring old, jstring new, jstring patch)
{
    int argc = 4;
    char *argv[argc];
    argv[0] = "bsdiff";
    argv[1] = (char *)((*env) -> GetStringUTFChars(env, old, 0));
    argv[2] = (char *)((*env) -> GetStringUTFChars(env, new, 0));
    argv[3] = (char *)((*env) -> GetStringUTFChars(env, patch, 0));
    int result = fileDiff(argc, argv);
    //释放资源
    (*env) -> ReleaseStringUTFChars(env,old,argv[1]);
    (*env) -> ReleaseStringUTFChars(env,new,argv[2]);
    (*env) -> ReleaseStringUTFChars(env,patch,argv[3]);
    return result;
}
```

进入 jni 目录执行命令 `sh /Users/hjy/Library/Android/sdk/ndk/21.3.6528147/ndk-build` 进行编译，就会在工程中生成 `libs` 目录，这里就是编译好的静态库了。

