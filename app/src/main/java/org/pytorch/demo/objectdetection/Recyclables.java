package org.pytorch.demo.objectdetection;

import java.util.stream.Stream;

public enum Recyclables {
    DEFAULT(-1,
            R.mipmap.attention,
            R.string.no_recyclable,
            0,
            0),
    ALUMINUM(0,
            R.mipmap.aluminium,
            R.string.aluminum,
            R.string.aluminum_collectables,
            R.string.aluminum_collection_reason),
    CARDBOARD(1,
            R.mipmap.cardboard,
            R.string.cardboard,
            R.string.cardboard_collectables,
            R.string.cardboard_collection_reason);

    final private int classIndex;
    final private int classImage;
    final private int classHeader;
    final private int whatRecycle;
    final private int whyRecycle;

    public int getIndex(){
        return this.classIndex;
    }

    public int getImage(){
        return this.classImage;
    }

    public int getHeader(){
        return this.classHeader;
    }

    public int getWhatRecycle(){
        return this.whatRecycle;
    }

    public int getWhyRecycle(){
        return this.whyRecycle;
    }

    public static Recyclables getRecyclableByIndex(int index){
        return Stream.of(Recyclables.values())
                .filter(recyclables -> recyclables.getIndex() == index)
                .findFirst()
                .orElse(DEFAULT);
    }

    Recyclables(final int classIndex, int classImage, int classHeader, int whatRecycle, int whyRecycle){
        this.classIndex = classIndex;
        this.classImage = classImage;
        this.classHeader = classHeader;
        this.whatRecycle = whatRecycle;
        this.whyRecycle = whyRecycle;
    }
}
