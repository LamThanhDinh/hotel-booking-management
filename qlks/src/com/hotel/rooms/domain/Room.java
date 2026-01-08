package com.hotel.rooms.domain;

import java.util.Objects;

public class Room {
    private final String id;
    private final String name;
    private final RoomType roomType;
    private final BedType bedType;
    private final Money price;
    private RoomStatus status;

    public Room(String id, String name, RoomType roomType, BedType bedType, Money price, RoomStatus status) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.roomType = Objects.requireNonNull(roomType, "roomType");
        this.bedType = Objects.requireNonNull(bedType, "bedType");
        this.price = Objects.requireNonNull(price, "price");
        this.status = Objects.requireNonNull(status, "status");
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public BedType getBedType() {
        return bedType;
    }

    public Money getPrice() {
        return price;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public boolean isAvailable() {
        return status == RoomStatus.TRONG;
    }

    public void markBooked() {
        this.status = RoomStatus.DA_DAT;
    }

    public void markInUse() {
        this.status = RoomStatus.DANG_SU_DUNG;
    }

    public void markReturned() {
        this.status = RoomStatus.DA_TRA;
    }

    public void markEmpty() {
        this.status = RoomStatus.TRONG;
    }
}