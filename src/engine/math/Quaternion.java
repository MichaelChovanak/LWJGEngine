/**
 * @file Quaternion.java
 * @author Michael Chovanak
 * @date Aug 25, 2020
 *
 */

package engine.math;


public class Quaternion {
	public float x;
	public float y;
	public float z;
	public float w;
	
	/**
	 * @param x
	 * @param y
	 * @param z
	 * @param w
	 */
	public Quaternion(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
	
	public float length() {
		return (float)Math.sqrt(lengthSquared());
	}
	
	public float lengthSquared() {
		return x*x + y*y + z*z + w*w;
	}
	
	public Quaternion normalize() {
		float length = length();
		
		x /= length;
		y /= length;
		z /= length;
		w /= length;
		return this;
	}
	
	public Quaternion conjugate() {
		return new Quaternion(-x, -y, -z, w);
	}
	
	public Quaternion mult(Quaternion r) {
		float w_ = w * r.w - x * r.x - y * r.y - z * r.z;
		float x_ = x * r.w + w * r.x + y * r.z - z * r.y;
		float y_ = y * r.w + w * r.y + z * r.x - x * r.z;
		float z_ = z * r.w + w * r.z + x * r.y - y * r.x;
		
		return new Quaternion(x_, y_, z_, w_);
	}
	
	public Quaternion mult(Vector3f r) {
		float w_ = -x * r.x - y * r.y - z * r.z;
		float x_ =  w * r.x + y * r.z - z * r.y;
		float y_ =  w * r.y + z * r.x - x * r.z;
		float z_ =  w * r.z + x * r.y - y * r.x;
		
		return new Quaternion(x_, y_, z_, w_);
	}
}
