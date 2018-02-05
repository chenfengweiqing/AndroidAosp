package com.android.systemui.statusbar.controller;

import com.android.systemui.R;

/**
 * @author lcz
 * @date 17-12-4
 */

public class NavigationUtils {

    private NavigationUtils() {

    }

    /**
     * @param type
     * @return getIconResource
     */
    public static int getIconResource(final int type) {
        switch (type) {
            case IconType.NONE:
                return R.drawable.ic_straight;
            case IconType.DEFAULT:
                return R.drawable.ic_straight;
            case IconType.LEFT:
                return R.drawable.ic_left;
            case IconType.RIGHT:
                return R.drawable.ic_right;
            case IconType.LEFT_FRONT:
                return R.drawable.ic_left_handed;
            case IconType.RIGHT_FRONT:
                return R.drawable.ic_right_handed;
            case IconType.LEFT_BACK:
                return R.drawable.ic_left_back;
            case IconType.RIGHT_BACK:
                return R.drawable.ic_right_back;
            case IconType.LEFT_TURN_AROUND:
                return R.drawable.ic_left_round;
            case IconType.STRAIGHT:
                return R.drawable.ic_straight;
            case IconType.ARRIVED_WAYPOINT:
                return R.drawable.ic_approach;
            case IconType.ENTER_ROUNDABOUT:
                return R.drawable.ic_roundabout_in;
            case IconType.OUT_ROUNDABOUT:
                return R.drawable.ic_roundabout_out;
            case IconType.ARRIVED_SERVICE_AREA:
                return R.drawable.ic_park;
            case IconType.ARRIVED_TOLLGATE:
                return R.drawable.ic_straight;
            case IconType.ARRIVED_DESTINATION:
                return R.drawable.ic_destination;
            case IconType.ARRIVED_TUNNEL:
                return R.drawable.ic_tunnel;
            case IconType.CROSSWALK:
                return R.drawable.ic_straight;
            case IconType.OVERPASS:
                return R.drawable.ic_straight;
            case IconType.UNDERPASS:
                return R.drawable.ic_straight;
            case IconType.SQUARE:
                return R.drawable.ic_straight;
            case IconType.PARK:
                return R.drawable.ic_park;
            case IconType.STAIRCASE:
                return R.drawable.ic_straight;
            case IconType.LIFT:
                return R.drawable.ic_straight;
            case IconType.CABLEWAY:
                return R.drawable.ic_straight;
            case IconType.SKY_CHANNEL:
                return R.drawable.ic_straight;
            case IconType.CHANNEL:
                return R.drawable.ic_straight;
            case IconType.WALK_ROAD:
                return R.drawable.ic_straight;
            case IconType.CRUISE_ROUTE:
                return R.drawable.ic_straight;
            case IconType.SIGHTSEEING_BUSLINE:
                return R.drawable.ic_straight;
            case IconType.SLIDEWAY:
                return R.drawable.ic_straight;
            case IconType.LADDER:
                return R.drawable.ic_straight;
            case IconType.Merge_Left:
                return R.drawable.ic_left_handed;
            case IconType.Merge_Right:
                return R.drawable.ic_right_handed;
            case IconType.Slow:
                return R.drawable.ic_straight;
            default:
                return R.drawable.ic_straight;
        }
    }

    private class IconType {
        public static final int NONE = 0;
        public static final int DEFAULT = 1;
        public static final int LEFT = 2;
        public static final int RIGHT = 3;
        public static final int LEFT_FRONT = 4;
        public static final int RIGHT_FRONT = 5;
        public static final int LEFT_BACK = 6;
        public static final int RIGHT_BACK = 7;
        public static final int LEFT_TURN_AROUND = 8;
        public static final int STRAIGHT = 9;
        public static final int ARRIVED_WAYPOINT = 10;
        public static final int ENTER_ROUNDABOUT = 11;
        public static final int OUT_ROUNDABOUT = 12;
        public static final int ARRIVED_SERVICE_AREA = 13;
        public static final int ARRIVED_TOLLGATE = 14;
        public static final int ARRIVED_DESTINATION = 15;
        public static final int ARRIVED_TUNNEL = 16;
        public static final int CROSSWALK = 17;
        public static final int OVERPASS = 18;
        public static final int UNDERPASS = 19;
        public static final int SQUARE = 20;
        public static final int PARK = 21;
        public static final int STAIRCASE = 22;
        public static final int LIFT = 23;
        public static final int CABLEWAY = 24;
        public static final int SKY_CHANNEL = 25;
        public static final int CHANNEL = 26;
        public static final int WALK_ROAD = 27;
        public static final int CRUISE_ROUTE = 28;
        public static final int SIGHTSEEING_BUSLINE = 29;
        public static final int SLIDEWAY = 30;
        public static final int LADDER = 31;
        public static final int Merge_Left = 51;
        public static final int Merge_Right = 52;
        public static final int Slow = 53;

        public IconType() {
        }
    }
}
