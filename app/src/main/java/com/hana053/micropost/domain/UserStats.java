package com.hana053.micropost.domain;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

import java.io.Serializable;

import lombok.Value;

@Value
@Parcel
public final class UserStats implements Serializable {

    static final long serialVersionUID = 1L;

    public final int micropostCnt;
    public final int followingCnt;
    public final int followerCnt;

    @ParcelConstructor
    public UserStats(int micropostCnt, int followingCnt, int followerCnt) {
        this.micropostCnt = micropostCnt;
        this.followingCnt = followingCnt;
        this.followerCnt = followerCnt;
    }

    public UserStats() {
        this(0, 0, 0);
    }
}
