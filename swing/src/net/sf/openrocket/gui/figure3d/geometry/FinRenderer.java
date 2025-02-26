package net.sf.openrocket.gui.figure3d.geometry;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUtessellator;
import com.jogamp.opengl.glu.GLUtessellatorCallback;
import com.jogamp.opengl.glu.GLUtessellatorCallbackAdapter;

import net.sf.openrocket.rocketcomponent.EllipticalFinSet;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.InsideColorComponent;
import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.BoundingBox;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.gui.figure3d.geometry.Geometry.Surface;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FinRenderer {
	private GLUtessellator tess = GLU.gluNewTess();
	
	public void renderFinSet(final GL2 gl, FinSet finSet, Surface which) {
		System.out.println("finset...");
		ArrayList<double[]> outerPoints =  new ArrayList<>();
		
	    BoundingBox bounds = finSet.getInstanceBoundingBox();
		gl.glMatrixMode(GL.GL_TEXTURE);
		gl.glPushMatrix();
		// Mirror the right side fin texture to avoid e.g. mirrored decal text
		if (which == Surface.INSIDE && ((InsideColorComponent) finSet).getInsideColorComponentHandler().isSeparateInsideOutside()) {
			gl.glScaled(-1 / (bounds.max.x - bounds.min.x), 1 / (bounds.max.y - bounds.min.y), 0);
		}
		else {
			gl.glScaled(1 / (bounds.max.x - bounds.min.x), 1 / (bounds.max.y - bounds.min.y), 0);
		}
		gl.glTranslated(-bounds.min.x, -bounds.min.y - finSet.getBodyRadius(), 0);
		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);

		Coordinate[] finPoints = finSet.getFinPointsWithLowResRoot();
		Coordinate[] tabPoints = finSet.getTabPointsWithRootLowRes();


		{
		    gl.glPushMatrix();

            gl.glTranslated(0, - finSet.getBodyRadius(), 0);		// Move to the parent centerline
            
            gl.glRotated( Math.toDegrees(finSet.getCantAngle()), 0, 1, 0);
            GLUtessellatorCallback cb = new GLUtessellatorCallbackAdapter() {
				@Override
				public void vertex(Object vertexData) {
					double[] d = (double[]) vertexData;
					gl.glTexCoord2d(d[0], d[1]);
					gl.glVertex3dv(d, 0);
				}
				
				@Override
				public void begin(int type) {
					gl.glBegin(type);
				}
				
				@Override
				public void end() {
					gl.glEnd();
				}

				@Override
				public void combine(double[] coords, Object[] data, float[] weight, Object[] outData) {
					double[] vertex = new double[3];
					vertex[0] = coords[0];
					vertex[1] = coords[1];
					vertex[2] = coords[2];
					outData[0] = vertex;
				}
			};
			
			GLU.gluTessCallback(tess, GLU.GLU_TESS_VERTEX, cb);
			GLU.gluTessCallback(tess, GLU.GLU_TESS_BEGIN, cb);
			GLU.gluTessCallback(tess, GLU.GLU_TESS_END, cb);
			GLU.gluTessCallback(tess, GLU.GLU_TESS_COMBINE, cb);
			// 内表面
			// fin side: +z
			//绕z轴的两侧 右侧
			if (finSet.getSpan() > 0 && which == Surface.INSIDE) {		// Right side
				GLU.gluTessBeginPolygon(tess, null);
				GLU.gluTessBeginContour(tess);
				gl.glNormal3f(0, 0, 1);
				for (int i = finPoints.length - 1; i >= 0; i--) {
					Coordinate c = finPoints[i];
					double[] p = new double[]{c.x, c.y + finSet.getBodyRadius(),
							c.z + finSet.getThickness() / 2.0};
					outerPoints.add(p); // 存储外表点

					GLU.gluTessVertex(tess, p, 0, p);
				}
				GLU.gluTessEndContour(tess);
				GLU.gluTessEndPolygon(tess);
			}
			// tab side: +z
			if (finSet.getTabHeight() > 0 && finSet.getTabLength() > 0 && which == Surface.INSIDE) {		// Right side
				GLU.gluTessBeginPolygon(tess, null);
				GLU.gluTessBeginContour(tess);
				gl.glNormal3f(0, 0, 1);
                for (Coordinate c : tabPoints) {
                    double[] p = new double[]{c.x, c.y + finSet.getBodyRadius(),
                            c.z + finSet.getThickness() / 2.0};
                    GLU.gluTessVertex(tess, p, 0, p);
                }
				GLU.gluTessEndContour(tess);
				GLU.gluTessEndPolygon(tess);
			}
			//外表面
			// fin side: -z
			if (finSet.getSpan() > 0 && which == Surface.OUTSIDE) {		// Left side
				GLU.gluTessBeginPolygon(tess, null);
				GLU.gluTessBeginContour(tess);
				gl.glNormal3f(0, 0, -1);
				for (Coordinate c : finPoints) {
					double[] p = new double[]{c.x, c.y + finSet.getBodyRadius(),
							c.z - finSet.getThickness() / 2.0};
					GLU.gluTessVertex(tess, p, 0, p);

//					outerPoints.add(p); // 存储外表点
				}
				GLU.gluTessEndContour(tess);
				GLU.gluTessEndPolygon(tess);
			}
			// tab side: -z
			if (finSet.getTabHeight() > 0 && finSet.getTabLength() > 0 && which == Surface.OUTSIDE) {		// Left side
				GLU.gluTessBeginPolygon(tess, null);
				GLU.gluTessBeginContour(tess);
				gl.glNormal3f(0, 0, -1);
				for (int i = tabPoints.length - 1; i >= 0; i--) {
					Coordinate c = tabPoints[i];
					double[] p = new double[]{c.x, c.y + finSet.getBodyRadius(),
							c.z - finSet.getThickness() / 2.0};
					GLU.gluTessVertex(tess, p, 0, p);
//					outerPoints.add(p); // 存储外表点

				}
				GLU.gluTessEndContour(tess);
				GLU.gluTessEndPolygon(tess);
			}

			// delete tessellator after processing
			GLU.gluDeleteTess(tess);
			
			// Fin strip around the edge
			if (finSet.getSpan() > 0 && which == Surface.EDGES) {
				if (!(finSet instanceof EllipticalFinSet))
					gl.glShadeModel(GLLightingFunc.GL_FLAT);
				gl.glBegin(GL.GL_TRIANGLE_STRIP);
				for (int i = 0; i <= finPoints.length; i++) {
					Coordinate c = finPoints[i % finPoints.length];
					// if ( i > 1 ){
					Coordinate c2 = finPoints[(i - 1 + finPoints.length)
							% finPoints.length];
					gl.glNormal3d(c2.y - c.y, c.x - c2.x, 0);
					// }
					gl.glTexCoord2d(c.x, c.y + finSet.getBodyRadius());
					gl.glVertex3d(c.x, c.y + finSet.getBodyRadius(),
							c.z - finSet.getThickness() / 2.0);
					gl.glVertex3d(c.x, c.y + finSet.getBodyRadius(),
							c.z + finSet.getThickness() / 2.0);
				}
				gl.glEnd();
			}
			// Tab strip around the edge
			if (finSet.getTabHeight() > 0 && finSet.getTabLength() > 0 && which == Surface.EDGES) {
				if (!(finSet instanceof EllipticalFinSet))
					gl.glShadeModel(GLLightingFunc.GL_FLAT);
				gl.glBegin(GL.GL_TRIANGLE_STRIP);
				for (int i = tabPoints.length; i >= 0; i--) {
					Coordinate c = tabPoints[i % tabPoints.length];
					// if ( i > 1 ){
					Coordinate c2 = tabPoints[(i - 1 + tabPoints.length)
							% tabPoints.length];
					gl.glNormal3d(c2.y - c.y, c.x - c2.x, 0);
					// }
					gl.glTexCoord2d(c.x, c.y + finSet.getBodyRadius());
					gl.glVertex3d(c.x, c.y + finSet.getBodyRadius(),
							c.z - finSet.getThickness() / 2.0);
					gl.glVertex3d(c.x, c.y + finSet.getBodyRadius(),
							c.z + finSet.getThickness() / 2.0);
				}
				gl.glEnd();
			}
			if (!(finSet instanceof EllipticalFinSet))
				gl.glShadeModel(GLLightingFunc.GL_SMOOTH);
			
			gl.glPopMatrix();
		}
		
		gl.glMatrixMode(GL.GL_TEXTURE);
		gl.glPopMatrix();
		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
	//	generateWings(outerPoints,finSet.getThickness(),0.03,finSet.getFinCount(),-bounds.min.x,-bounds.min.y,finSet.getBodyRadius());
		
	}
	public static void generateWings(List<double[]> coordinates, double thickness, double density, int count,double x,double y,double bodyRadius) {
		if (coordinates.size()==0)return;
		 applyTransformation(coordinates, x, y, bodyRadius);

		// 定义文件路径
		String filePath = "E://finset.txt";

		// 构造底面点
		List<double[]> bottomSurface = new ArrayList<>();
		for (double[] point : coordinates) {
			bottomSurface.add(new double[]{point[0], point[1], point[2] - thickness});
		}

		// 计算旋转角度间隔
		double angleStep = 360.0 / count;

		// 存储所有机翼的密集化点
		List<double[]> allPoints = new ArrayList<>();

		// 遍历每个机翼
		for (int i = 0; i < count; i++) {
			double angle = Math.toRadians(i * angleStep);
			List<double[]> rotatedTop = rotateWing(coordinates, angle);
			List<double[]> rotatedBottom = rotateWing(bottomSurface, angle);

			// 密集化顶面和底面
			allPoints.addAll(gridifySurface(rotatedTop, density));
			allPoints.addAll(gridifySurface(rotatedBottom, density));

			// 密集化侧面
			for (int j = 0; j < rotatedTop.size(); j++) {
				double[] top1 = rotatedTop.get(j);
				double[] bottom1 = rotatedBottom.get(j);
				double[] top2 = rotatedTop.get((j + 1) % rotatedTop.size());
				double[] bottom2 = rotatedBottom.get((j + 1) % rotatedBottom.size());
				allPoints.addAll(gridifySide(top1, top2, bottom1, bottom2, 0.01));
			}
		}

		// 保存点到文件
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
			for (double[] point : allPoints) {
				writer.write(String.format("(%f, %f, %f)\n", point[0], point[1], point[2]));
			}
			System.out.println("Multi-wing structure saved to " + filePath);
		} catch (IOException e) {
			System.err.println("Error writing to file: " + e.getMessage());
		}
	}

	// 绕y轴旋转
	private static List<double[]> rotateWing(List<double[]> wing, double angle) {
		List<double[]> rotatedWing = new ArrayList<>();
		double cosTheta = Math.cos(angle);
		double sinTheta = Math.sin(angle);
		for (double[] point : wing) {
			double x = point[0];
			double y = point[1];
			double z = point[2];
			double newX = cosTheta * x + sinTheta * z;
			double newZ = -sinTheta * x + cosTheta * z;
			rotatedWing.add(new double[]{newX, y, newZ});
		}
		return rotatedWing;
	}

	// 顶面和底面密集化
	private static List<double[]> gridifySurface(List<double[]> surface, double density) {
		List<double[]> gridPoints = new ArrayList<>();
		double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
		double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;

		for (double[] point : surface) {
			minX = Math.min(minX, point[0]);
			maxX = Math.max(maxX, point[0]);
			minY = Math.min(minY, point[1]);
			maxY = Math.max(maxY, point[1]);
		}

		for (double x = minX; x <= maxX; x += density) {
			for (double y = minY; y <= maxY; y += density) {
				if (isInsidePolygon(x, y, surface)) {
					double z = surface.get(0)[2];
					gridPoints.add(new double[]{x, y, z});
				}
			}
		}
		return gridPoints;
	}

	// 判断点是否在多边形内
	private static boolean isInsidePolygon(double x, double y, List<double[]> polygon) {
		int intersections = 0;
		for (int i = 0; i < polygon.size(); i++) {
			double[] p1 = polygon.get(i);
			double[] p2 = polygon.get((i + 1) % polygon.size());
			if ((p1[1] > y) != (p2[1] > y) &&
					x < (p2[0] - p1[0]) * (y - p1[1]) / (p2[1] - p1[1]) + p1[0]) {
				intersections++;
			}
		}
		return (intersections % 2) != 0;
	}

	// 侧面密集化
	private static List<double[]> gridifySide(double[] top1, double[] top2, double[] bottom1, double[] bottom2, double density) {
		List<double[]> gridPoints = new ArrayList<>();
		for (double t1 = 0; t1 <= 1; t1 += density) {
			for (double t2 = 0; t2 <= 1; t2 += density) {
				double[] point = new double[]{
						(1 - t1) * ((1 - t2) * top1[0] + t2 * top2[0]) + t1 * ((1 - t2) * bottom1[0] + t2 * bottom2[0]),
						(1 - t1) * ((1 - t2) * top1[1] + t2 * top2[1]) + t1 * ((1 - t2) * bottom1[1] + t2 * bottom2[1]),
						(1 - t1) * ((1 - t2) * top1[2] + t2 * top2[2]) + t1 * ((1 - t2) * bottom1[2] + t2 * bottom2[2])
				};
				gridPoints.add(point);
			}
		}
		return gridPoints;
	}

	// 应用平移变换
	public static void applyTransformation(List<double[]> coordinates, double minX, double minY, double bodyRadius) {
		// 遍历原数组中的每个点，直接修改
		for (double[] point : coordinates) {
			// 应用第一步平移
			point[0] -= minX;
			point[1] -= minY + bodyRadius;

			// 应用第二步平移
			point[1] -= bodyRadius;
		}
	}


}
