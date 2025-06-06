package ua.myxazaur.vintagecam.utils;

public class Vector3d {
    public double x, y, z;

    public Vector3d() {
        this.x = 0.0;
        this.y = 0.0;
        this.z = 0.0;
    }

    public Vector3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3d set(Vector3d other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
        return this;
    }

    public Vector3d set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vector3d add(Vector3d other) {
        this.x += other.x;
        this.y += other.y;
        this.z += other.z;
        return this;
    }

    public Vector3d mul(Vector3d other, Vector3d dest) {
        dest.x = this.x * other.x;
        dest.y = this.y * other.y;
        dest.z = this.z * other.z;
        return dest;
    }

    public boolean equals(Vector3d other) {
        return this.x == other.x && this.y == other.y && this.z == other.z;
    }
}