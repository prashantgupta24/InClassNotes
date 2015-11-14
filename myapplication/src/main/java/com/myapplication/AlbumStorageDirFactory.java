package com.myapplication;

/**
 * Created by juzer_000 on 11/14/2015.
 */
import java.io.File;

abstract class AlbumStorageDirFactory {
    public abstract File getAlbumStorageDir(String albumName);
}
