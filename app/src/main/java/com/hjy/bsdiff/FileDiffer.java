package com.hjy.bsdiff;

public class FileDiffer {

    static {
        System.loadLibrary("bsdiff");
        System.loadLibrary("bspatch");
    }

    /**
     * 生成差分包
     *
     * @param oldFile
     * @param newFile
     * @param patchFile
     * @return
     */
    public static native int fileDiff(String oldFile, String newFile, String patchFile);

    /**
     * 将老包与差分包合并成新包
     *
     * @param oldFile
     * @param newFile
     * @param patchFile
     * @return
     */
    public static native int fileCombine(String oldFile, String newFile, String patchFile);

}
