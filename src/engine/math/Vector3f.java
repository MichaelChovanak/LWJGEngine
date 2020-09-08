package engine.math;

public class Vector3f {

	public float x;
	public float y;
	public float z;

	public Vector3f() {
	}

	public Vector3f(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector3f set(Vector3f v) {
		x = v.x;
		y = v.y;
		z = v.z;
		return this;
	}

	public void set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector3f add(Vector3f v) {
		x += v.x;
		y += v.y;
		z += v.z;
		return this;
	}
	
	public Vector3f sub(Vector3f v) {
		x -= v.x;
		y -= v.y;
		z -= v.z;
		return this;
	}
	
	public Vector3f mult (float f) {
		return mult(new Vector3f(f,f,f));
	}
	
	public Vector3f mult(Vector3f v) {
		x *= v.x;
		y *= v.y;
		z *= v.z;
		return this;
	}

	public float lengthSquared() {
		return x * x + y * y + z * z;
	}

	public float length() {
		return (float) Math.sqrt(lengthSquared());
	}

	public void normalize() {
		float d = length();
		x /= d;
		y /= d;
		z /= d;
	}

	public void cross(Vector3f v1, Vector3f v2) {
		set(v1.y * v2.z - v1.z * v2.y, v1.z * v2.x - v1.x * v2.z, v1.x * v2.y - v1.y * v2.x);
	}
	
	public Vector3f rotate(float angle, Vector3f axis) {
		float sinHalfAngle = (float)Math.sin(Math.toRadians(angle / 2));
		float cosHalfAngle = (float)Math.cos(Math.toRadians(angle / 2));
		
		float rX = axis.x * sinHalfAngle;
		float rY = axis.y * sinHalfAngle;
		float rZ = axis.z * sinHalfAngle;
		float rW = cosHalfAngle;
	
		Quaternion rotation = new Quaternion(rX, rY, rZ, rW);
		Quaternion conjugate = rotation.conjugate();
		Quaternion fin = rotation.mult(this).mult(conjugate);
		
		x = fin.x;
		y = fin.y;
		z = fin.z;
		
		return this;
	}

}
