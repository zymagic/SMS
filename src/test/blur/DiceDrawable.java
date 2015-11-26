package test.blur;

import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;

public class DiceDrawable extends Drawable {

    private Paint mPaint = new Paint();

    private Polygon polygon1, polygon2;

    private PointF target1 = new PointF();
    private PointF target2 = new PointF();
    private Paint targetPaint = new Paint();

    private PointF tempPoint = new PointF();
    private PointF tempPoint2 = new PointF();
    private Rect tempRect = new Rect();

    private Solution solution1 = new Solution();
    private Solution solution2 = new Solution();

    private boolean useSolution = false;

    private static final int STATE_REST = 0;
    private static final int STATE_MOVING = 1;
    private static final int STATE_SETTLING = 2;
    private int state = STATE_REST;

    private Bitmap diceClear, diceBlur;

    private SparseArray<Bitmap> bitmapCache = new SparseArray<Bitmap>();

    private Random diceRandom;

    private Context context;

    private Dice dice1, dice2;

    private DisplayMotion displayMotion = new DisplayMotion();

    private AnimationCallback mCallback;

    private Path path = new Path();

    private Paint pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private RectF line1 = new RectF();
    private RectF line2 = new RectF();

    private Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public DiceDrawable(Context context) {
    	this.context = context;

    	diceClear = BitmapFactory.decodeResource(context.getResources(), R.drawable.dice_clear);
    	diceBlur = BitmapFactory.decodeResource(context.getResources(), R.drawable.dice_blur);

        mPaint.setColor(0xccffff00);
        mPaint.setStyle(Paint.Style.STROKE);

        float radius = context.getResources().getDisplayMetrics().density * 40f;
        polygon1 = new Polygon(6).setRadius(radius);
        polygon2 = new Polygon(5).setRadius(radius);

        dice1 = new Dice();
        dice1.polygon = polygon1;

        dice2 = new Dice();
        dice2.polygon = polygon2;

        targetPaint.setColor(0x80ff0000);
        targetPaint.setStyle(Paint.Style.FILL);

        diceRandom = new Random();
        startMotion();

        pathPaint.setStyle(Paint.Style.FILL);
        pathPaint.setColor(0xffff0000);

        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeWidth(40);
        linePaint.setColor(0xffff0000);
    }

    private void startMotion() {
    	dice1.chooseBitmap(context, diceRandom.nextInt(6) + 1);
    	dice2.chooseBitmap(context, diceRandom.nextInt(6) + 1);
    	polygon1.startRotate((float) (Math.pow(-1, diceRandom.nextInt()) * 45)).startMove(-10 - diceRandom.nextInt(6), -5 - diceRandom.nextInt(7));
    	polygon2.startRotate((float) (Math.pow(-1, diceRandom.nextInt()) * 45)).startMove(-7 - diceRandom.nextInt(5), -20 - diceRandom.nextInt(5));
    	state = STATE_MOVING;
    	invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        if (state == 0 && displayMotion.update()) {
            invalidateSelf();
        }

    	dice1.draw(canvas);
    	dice2.draw(canvas);

        next();

        canvas.drawCircle(target1.x, target1.y, getBounds().width() / 50f, targetPaint);
        canvas.drawCircle(target2.x, target2.y, getBounds().width() / 50f, targetPaint);

        canvas.drawPath(path, pathPaint);
        canvas.drawLine(line1.left, line1.top, line1.right, line1.bottom, linePaint);
        canvas.drawLine(line2.left, line2.top, line2.right, line2.bottom, linePaint);
    }

    private void next() {
        if (state == 0) {
            return;
        }

        Rect bounds = getBounds();
        if (state == 1) {
            if (polygon1.updateMotion() | polygon2.updateMotion()) {
                invalidateSelf();
            } else {
                state = 0;
                finishAnimation();
            }
        } else {
        	if (updateWithFriction()) {
        		invalidateSelf();
        	} else {
        		state = 0;
        		finishAnimation();
        	}
        }

        if ((state == 1 && (polygon1.checkReflect(bounds.left, bounds.top, bounds.right, bounds.bottom)
                | polygon2.checkReflect(bounds.left, bounds.top, bounds.right, bounds.bottom)))
                | Polygon.hit(polygon1, polygon2)) {
            evalTarget();
        }
    }

    private boolean updateWithFriction() {
        if (useSolution) {
            return updateRotation(polygon1, !updatePositionWithSolution(polygon1, solution1)) | updateRotation(polygon2, !updatePositionWithSolution(polygon2, solution2));
        }
        return polygon1.updateMotionWithFriction() | polygon2.updateMotionWithFriction();
    }

    private boolean updatePositionWithSolution(Polygon p, Solution s) {
    	PointF point = p.center;
    	float v = (float) Math.hypot(p.moveSpeed.x, p.moveSpeed.y);
    	int frame = s.frames++;
    	switch (s.hitCount) {
    	case 0:
    		if (interpolate(point, s.start, s.target, v, frame)) {
    			return true;
    		} else {
    			p.moveSpeed.set(0, 0);
    			return false;
    		}
    	case 1:
    		if (s.currentHit == 0) {
    			if(!interpolate(point, s.start, s.firstHit, v, frame)) {
    				updateSpeed(p.moveSpeed, s.target.x - s.firstHit.x, s.target.y - s.firstHit.y);
    				s.currentHit = 1;
    				s.frames = 0;
    			}
    		} else if (s.currentHit == 1) {
    			if (interpolate(point, s.firstHit, s.target, v, frame)) {
    				return true;
    			} else {
    				p.moveSpeed.set(0, 0);
    				return false;
    			}
    		}
    		break;
    	case 2:
    		if (s.currentHit == 0) {
    			if (!interpolate(point, s.start, s.firstHit, v, frame)) {
    				updateSpeed(p.moveSpeed, s.secondHit.x - s.firstHit.x, s.secondHit.y - s.firstHit.y);
    				s.currentHit = 1;
    				s.frames = 0;
    			}
    		} else if (s.currentHit == 1) {
    			if (!interpolate(point, s.firstHit, s.secondHit, v, frame)) {
    				updateSpeed(p.moveSpeed, s.target.x - s.secondHit.x, s.target.y - s.secondHit.y);
    				s.currentHit = 2;
    				s.frames = 0;
    			}
    		} else if (s.currentHit == 2) {
    			if (interpolate(point, s.secondHit, s.target, v, frame)) {
    				return true;
    			} else {
    				p.moveSpeed.set(0, 0);
    				return false;
    			}
    		}
    		break;
    	default:
    		return false;
    	}
    	return true;
    }

    private boolean updateRotation(Polygon p, boolean stop) {
        if (Math.abs(p.rotateSpeed) <= 1f) {
            return false;
        }

    	p.degree += p.rotateSpeed;
    	if (stop) {
    		p.rotateSpeed = p.rotateSpeed > 0 ? p.rotateSpeed - 1 : p.rotateSpeed + 1;
    		return Math.abs(p.rotateSpeed) > 1f;
    	}
    	return true;
    }

    private boolean interpolate(PointF p, PointF start, PointF end, float v, int frame) {
    	float totalFrames = (float) (v == 0 ? 300 / 17f : Math.hypot(end.x - start.x, end.y - start.y) / v);
    	totalFrames = Math.min(totalFrames, 500 / 17f);
    	if (totalFrames == 0) {
    		return false;
    	}
    	float ratio = frame / totalFrames;
    	boolean expired = ratio >= 1f;
    	if (ratio > 1f) {
    		ratio = 1f;
    	} else if (ratio < 0f) {
    		ratio = 0f;
    	}
    	p.set(start.x + (end.x - start.x) * ratio, start.y + (end.y - start.y) * ratio);
    	return !expired;
    }

    private void evalTarget() {
        if (state < 2) {
            return;
        }

        float da1 = (float) Math.hypot(polygon1.center.x - target1.x, polygon1.center.y - target1.y);
        float da2 = (float) Math.hypot(polygon1.center.x - target2.x, polygon1.center.y - target2.y);
        float db1 = (float) Math.hypot(polygon2.center.x - target1.x, polygon2.center.y - target1.y);
        float db2 = (float) Math.hypot(polygon2.center.x - target2.x, polygon2.center.y - target2.y);
        PointF t1, t2;

        if (da1 > db1 && da2 > db2) {
            t1 = db1 < db2 ? target1 : target2;
        } else if (da1 < db1 && da2 < db2) {
            t1 = da1 < da2 ? target2 : target1;
        } else {
            t1 = da1 + db2 < da2 + db1 ? target1 : target2;
        }
        t2 = t1 == target1 ? target2 : target1;

        evalSolution(t1, polygon1, solution1);
        evalSolution(t2, polygon2, solution2);
        evalCross();
    }

    private void evalSolution(PointF target, Polygon p, Solution s) {
    	s.start.set(p.center);
    	s.target.set(target);
    	s.currentHit = 0;
    	s.frames = 0;

        Rect bounds = getBounds();
        tempRect.set(bounds);
        tempRect.inset((int) p.radius, (int) p.radius);
        bounds = tempRect;

        // 检查是否需要碰撞边界
        if ((target.x - p.center.x) * p.moveSpeed.x + (target.y - p.center.y) * p.moveSpeed.y < 0) {
            s.hitCount = 1;
            if (p.moveSpeed.x * p.moveSpeed.y != 0) {
                if (p.moveSpeed.x > 0 && p.moveSpeed.y > 0) {
                	evalCorner(s, p, bounds.right, bounds.bottom, -1, 0, 0, -1);
                } else if (p.moveSpeed.x < 0 && p.moveSpeed.y < 0) {
                    evalCorner(s, p, bounds.left, bounds.top, 1, 0, 0, 1);
                } else if (p.moveSpeed.x > 0 && p.moveSpeed.y < 0) {
                    evalCorner(s, p, bounds.right, bounds.top, 0, 1, -1, 0);
                } else if (p.moveSpeed.x < 0 && p.moveSpeed.y > 0) {
                    evalCorner(s, p, bounds.left, bounds.bottom, 0, -1, 1, 0);
                }
            } else if (p.moveSpeed.x == 0) {
            	s.firstHit.set(p.center.x, p.moveSpeed.y > 0 ? bounds.bottom : bounds.top);
            } else if (p.moveSpeed.y == 0) {
            	s.firstHit.set(p.moveSpeed.x > 0 ? bounds.right : bounds.left, p.center.y);
            }
        } else {
        	s.hitCount = 0;
        	updateSpeed(p.moveSpeed, s.target.x - s.start.x, s.target.y - s.start.y);
        }
    }

    private void updateSpeed(PointF speed, float vx, float vy) {
    	float v = (float) Math.hypot(speed.x, speed.y);
    	speed.set(vx, vy);
    	normalize(speed, v);
    }

    private void normalize(PointF p, float length) {
    	float d = (float) Math.hypot(p.x, p.y);
    	if (d == 0) {
    		return;
    	}
    	p.set(p.x / d * length, p.y / d * length);
    }

    private void evalCorner(Solution s, Polygon p, float cornerX, float cornerY, float firstEdgeNormalX, float firstEdgeNormalY, float secondEdgeNormalX, float secondEdgeNormalY) {
    	// 计算第一个碰撞点
    	float edgeNormalX, edgeNormalY;
    	PointF cornerVector = tempPoint;
    	cornerVector.set(cornerX - p.center.x, cornerY - p.center.y);
    	tempPoint2.set(cornerX, cornerY);
        if (cornerVector.x * -p.moveSpeed.y + cornerVector.y * p.moveSpeed.x > 0) {
        	s.firstHit.set(calculateCross(p.center, p.moveSpeed.x, p.moveSpeed.y, tempPoint2, firstEdgeNormalY, firstEdgeNormalX, tempPoint));
            edgeNormalX = firstEdgeNormalX;
            edgeNormalY = firstEdgeNormalY;
        } else {
            s.firstHit.set(calculateCross(p.center, p.moveSpeed.x, p.moveSpeed.y, tempPoint2, secondEdgeNormalY, secondEdgeNormalX, tempPoint));
            edgeNormalX = secondEdgeNormalX;
            edgeNormalY = secondEdgeNormalY;
        }

        // 计算第二个碰撞点
        PointF reflect = tempPoint;
        reflect.set(s.firstHit.x - p.center.x, s.firstHit.y - p.center.y);
        reflectVector(reflect, edgeNormalX, edgeNormalY);
        if (((s.target.x - s.firstHit.x) * -edgeNormalY + (s.target.y - s.firstHit.y) * edgeNormalX) * (reflect.x * -edgeNormalY + reflect.y * edgeNormalX) < 0) {
        	s.hitCount = 2;
        	float mirrorX = 2 * cornerX - s.firstHit.x;
        	float mirrorY = 2 * cornerY - s.firstHit.y;
        	tempPoint.set(s.target.x - mirrorX, s.target.y - mirrorY);
        	float disX = Math.abs(cornerX - s.firstHit.x);
        	float disY = Math.abs(cornerY - s.firstHit.y);
        	if (disX != 0 && tempPoint.x != 0) {
        		float ratio = Math.abs(tempPoint.x) / disX;
        		s.secondHit.set(mirrorX + tempPoint.x / ratio, mirrorY + tempPoint.y / ratio);
        	} else if (disY != 0 && tempPoint.y != 0) {
        		float ratio = Math.abs(tempPoint.y) / disY;
        		s.secondHit.set(mirrorX + tempPoint.x / ratio, mirrorY + tempPoint.y / ratio);
        	} else {
        		s.secondHit.set(cornerX, cornerY);
        	}
        }
    }

    private void evalCross() {
    	PointF p1Start = solution1.hitCount == 0 ? solution1.start : solution1.hitCount == 1 ? solution1.firstHit : solution1.secondHit;
    	float v1x = solution1.target.x - p1Start.x;
    	float v1y = solution1.target.y - p1Start.y;

    	PointF p2Start = solution2.hitCount == 0 ? solution2.start : solution2.hitCount == 1 ? solution2.firstHit : solution2.secondHit;
    	float v2x = solution2.target.x - p2Start.x;
    	float v2y = solution2.target.y - p2Start.y;

    	PointF cross = calculateCross(p1Start, v1x, v1y, p2Start, v2x, v2y, tempPoint);
    	if (cross == null) {
    		return;
    	}

    	float crossX = cross.x;
    	float crossY = cross.y;

    	boolean crossed = (crossX - p1Start.x) * (crossX - solution1.target.x) + (crossY - p1Start.y) * (crossY - solution1.target.y) <= 0
    			&& (crossX - p2Start.x) * (crossX - solution2.target.x) + (crossY - p2Start.y) * (crossY - solution2.target.y) <= 0;

    	if (crossed) {
    		// cross point inside both of line segments, swap target
    		tempPoint.set(solution1.target);
    		solution1.target.set(solution2.target);
    		solution2.target.set(tempPoint);
    		if (solution1.hitCount == 0) {
    			updateSpeed(polygon1.moveSpeed, solution1.target.x - solution1.start.x, solution1.target.y - solution1.start.y);
    		}
    		if (solution2.hitCount == 0) {
    			updateSpeed(polygon2.moveSpeed, solution2.target.x - solution2.start.x, solution2.target.y - solution2.start.y);
    		}
    	}
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);

        polygon1.setPosition(bounds.right + polygon1.radius, bounds.height() / 2f);
        polygon2.setPosition(bounds.right + polygon2.radius, bounds.height() * 0.75f);

        Random r = new Random(47);
        target1.set(bounds.width() / 4f + r.nextInt(bounds.width() / 2), bounds.height() / 4f + r.nextInt(bounds.width() / 8));
        target2.set(bounds.width() / 4f + r.nextInt(bounds.width() / 4), bounds.height() / 2f + r.nextInt(bounds.width() / 4));

        displayMotion.init(bounds);

        float cx = bounds.exactCenterX();
        float cy = bounds.exactCenterY();

        float offsetInner = 20 / 200f;
        float offsetOuter = 20 / 400f;
        float offset1 = (float) Math.toRadians(6);
        float offset2 = (float) Math.toRadians(2.5f);
        float total = (float) Math.toRadians(30);
        float ai = (float) Math.toDegrees(offsetInner);
        float ao = (float) Math.toDegrees(offsetOuter);

        path.reset();
        path.moveTo((float) (cx + 200 * Math.cos(offset1 + offsetInner)), (float) (cy - 200 * Math.sin(offset1 + offsetInner)));
        path.arcTo(new RectF(cx - 200, cy - 200, cx + 200, cy + 200), -6 - ai, -18 + ai + ai);
        path.lineTo((float) (cx + 400 * Math.cos(total - offset1 - offset2 - offsetOuter)), (float) (cy - 400 * Math.sin(total - offset1 - offset2 - offsetOuter)));
        path.arcTo(new RectF(cx - 400, cy - 400, cx + 400, cy + 400), -21.5f + ao, 13f - ao - ao);
        path.close();

        line1.set((float) (cx + 220 * Math.cos(offset1 + offsetInner)), (float) (cy - 220 * Math.sin(offset1 + offsetInner)),
                (float) (cx + 380 * Math.cos(offset1 + offset2 + offsetOuter)), (float) (cy - 380 * Math.sin(offset1 + offset2 + offsetOuter)));
        line2.set((float) (cx + 220 * Math.cos(total - offset1 - offsetInner)), (float) (cy - 220 * Math.sin(total - offset1 - offsetInner)),
                (float) (cx + 380 * Math.cos(total - offset1 - offset2 - offsetOuter)), (float) (cy - 380 * Math.sin(total - offset1 - offset2 - offsetOuter)));

    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
    }

    @Override
    public int getOpacity() {
        return 0;
    }

    private static class Polygon {
        private final int edges;
        private PointF center = new PointF();
        private PointF moveSpeed = new PointF();
        private float radius;
        private float degree;
        private float rotateSpeed;

        public Polygon(int edges) {
            if (edges < 3) {
                throw new IllegalArgumentException("Polygon edges should not be less than 3");
            }
            this.edges = edges;
        }

        public Polygon setRadius(float radius) {
            this.radius = radius;
            return this;
        }

        public Polygon setPosition(float x, float y) {
            center.set(x, y);
            return this;
        }

//        public Polygon setRotation(float degree) {
//            this.degree = degree;
//            return this;
//        }

        public Polygon startRotate(float speed) {
            this.rotateSpeed = speed;
            return this;
        }

        public Polygon startMove(float speedx, float speedy) {
            this.moveSpeed.set(speedx, speedy);
            return this;
        }

        public boolean updateMotion() {
            degree += rotateSpeed;
            center.offset(moveSpeed.x, moveSpeed.y);
            return Math.abs(rotateSpeed) > 1 || Math.hypot(moveSpeed.x, moveSpeed.y) > 1f;
        }

        public boolean updateMotionWithFriction() {
            if (Math.abs(rotateSpeed) <= 1f) {
                return false;
            }
            rotateSpeed = rotateSpeed > 0 ? rotateSpeed - 1 : rotateSpeed + 1;
            degree += rotateSpeed;
            center.offset(moveSpeed.x, moveSpeed.y);
            moveSpeed.x *= 0.5f;
            moveSpeed.y *= 0.5f;
            return Math.abs(rotateSpeed) > 1;
        }

        static boolean hit(Polygon p1, Polygon p2) {
            boolean hit = Math.hypot(p1.center.x - p2.center.x, p1.center.y - p2.center.y) < p1.radius + p2.radius;
            if (hit) {
                float rx = p2.center.x - p1.center.x;
                float ry = p2.center.y - p1.center.y;
                float l = (float) Math.hypot(rx, ry);
                rx /= l;
                ry /= l;
                float s1 = p1.moveSpeed.x * rx + p1.moveSpeed.y * ry;
                float s2 = p2.moveSpeed.x * rx + p2.moveSpeed.y * ry;
                if (s1 <= s2) {
                    return false;
                }

                float sx = (s1 - s2) * rx;
                float sy = (s1 - s2) * ry;
                float n1x = p1.moveSpeed.x - sx;
                float n1y = p1.moveSpeed.y - sy;
                float n2x = p2.moveSpeed.x + sx;
                float n2y = p2.moveSpeed.y + sy;
                p1.moveSpeed.set(n1x, n1y);
                p2.moveSpeed.set(n2x, n2y);
                return true;
            }
            return false;
        }

        public boolean checkReflect(float left, float top, float right, float bottom) {
            boolean hitLeft = center.x - left < radius && moveSpeed.x < 0;
            boolean hitTop = center.y - top < radius && moveSpeed.y < 0;
            boolean hitRight = right - center.x < radius && moveSpeed.x > 0;
            boolean hitBottom = bottom - center.y < radius && moveSpeed.y > 0;
            if (hitLeft && hitTop || hitRight && hitBottom) {
                reflectVector(moveSpeed, right - left, bottom - top);
                return true;
            } else if (hitLeft && hitBottom || hitRight && hitTop) {
                reflectVector(moveSpeed, right - left, top - bottom);
                return true;
            }

            if (hitLeft || hitRight) {
                reflectVector(moveSpeed, 1, 0);
                return true;
            } else if (hitTop || hitBottom) {
                reflectVector(moveSpeed, 0, 1);
                return true;
            }
            return false;
        }
    }

    private static class Solution {
        int hitCount = 1;
        PointF start = new PointF();
        PointF firstHit = new PointF();
        PointF secondHit = new PointF();
        PointF target = new PointF();

        int frames;
        int currentHit;
//        boolean bezier = false;
//        PointF controlPoint = new PointF();
    }

    private static class DisplayMotion {
        PointF pos1 = new PointF();
        PointF pos2 = new PointF();
        PointF start1 = new PointF();
        PointF start2 = new PointF();
        float degree1, degree2;
        float totalFrames = 500 / 17f;
        int frames = 0;
        boolean started = false;

        Polygon left, right;

        public void init(Rect bounds) {
            pos1.set(bounds.left + bounds.width() / 4f, bounds.top + bounds.width() / 3f);
            pos2.set(bounds.right - bounds.width() / 4f, bounds.top + bounds.width() / 3f);
        }

        public void startMotion(Polygon p1, Polygon p2, int duration) {
            if (started) {
                return;
            }
            started = true;
            this.left = p1.center.x < p2.center.x ? p1 : p2;
            this.right = this.left == p1 ? p2 : p1;
            start1.set(this.left.center);
            start2.set(this.right.center);
            degree1 = this.left.degree;
            degree1 %= 360;
            degree1 = degree1 > 180 ? degree1 - 360 : degree1 < -180 ? degree1 + 360 : degree1;
            degree2 = this.right.degree;
            degree2 %= 360;
            degree2 = degree2 > 180 ? degree2 - 360 : degree2 < -180 ? degree2 + 360 : degree2;
            totalFrames = duration / 17f;
            frames = 0;
        }

        public void cancel() {
            started = false;
            left = null;
            right = null;
        }

        public boolean update() {
            if (!started) {
                return false;
            }

            float ratio = totalFrames <= 0 ? 1f : (frames++ / totalFrames);
            boolean expired = ratio > 1f;
            if (ratio >= 1f) {
                ratio = 1f;
            }

            left.center.x = start1.x + (pos1.x - start1.x) * ratio * ratio;
            left.center.y = start1.y + (pos1.y - start1.y) * ratio * ratio;
            left.degree = degree1 * (1 - ratio);

            right.center.x = start2.x + (pos2.x - start2.x) * ratio * ratio;
            right.center.y = start2.y + (pos2.y - start2.y) * ratio * ratio;
            right.degree = degree2 * (1 - ratio * ratio);

            if (expired) {
                started = false;
            }

            return !expired;
        }
    }

    static void reflectVector(PointF src, float nx, float ny) {
        if (Float.compare(nx * nx + ny * ny, 0f) == 0) {
            return;
        }

        if (-ny * src.x + nx * src.y < 0) {
            nx *= -1;
            ny *= -1;
        }

        float l = (float) Math.hypot(nx, ny);
        nx = nx / l;
        ny = ny / l;

        l = Math.abs(2 * src.x * -ny + 2 * src.y * nx);

        float lx = l * -ny - src.x;
        float ly = l * nx - src.y;

        src.set(lx, ly);
    }

    static PointF calculateCross(PointF p1, float r1x, float r1y, PointF p2, float r2x, float r2y, PointF out) {
    	float lv1 = (float) Math.hypot(r1x, r1y);
    	if (lv1 == 0) {
    		return null;
    	}

    	r1x /= lv1;
    	r1y /= lv1;

    	float lv2 = (float) Math.hypot(r2x, r2y);
    	if (lv2 == 0) {
    		return null;
    	}

    	r2x /= lv2;
    	r2y /= lv2;

    	float normalLen1 = (p2.x - p1.x) * -r2y + (p2.y - p1.y) * r2x;
    	float normalLen2 = r1x * -r2y + r1y * r2x;
    	if (normalLen2 == 0) {
    		// parallel
    		return null;
    	}

    	float crossX = p1.x + normalLen1 / normalLen2 * r1x;
    	float crossY = p1.y + normalLen1 / normalLen2 * r1y;
    	out.set(crossX, crossY);
    	return out;
    }

    // for test
    public void forward() {
        switch (state) {
            case STATE_REST:
                useSolution = !useSolution;
                start();
                break;
            case STATE_MOVING:
                stop();
                break;
        }
    }

    public void start() {
        if (state != STATE_REST) {
            return;
        }
        displayMotion.cancel();
        startMotion();
        invalidateSelf();
    }

    public void stop() {
        if (state != STATE_MOVING) {
            return;
        }
        state = STATE_SETTLING;
        evalTarget();
    }

    private class Dice {
    	Polygon polygon;

    	int dicePoint = -1;
    	Bitmap diceBitmap;

    	Paint alphaPaint = new Paint(Paint.ANTI_ALIAS_FLAG), secondPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    	public void draw(Canvas canvas) {
    		float speed = Math.abs(polygon.rotateSpeed);
    		Bitmap bmp = null;
    		Bitmap second = null;
    		float firstAlpha;
    		float secondAlpha;

    		if (diceBitmap == null) {
    			bmp = speed < 10 ? diceClear : diceBlur;
    			second = null;
    			firstAlpha = 1f;
    			secondAlpha = 0;
    		} else {
    			bmp = speed < 15 ? diceBitmap : speed < 20 ? diceClear : diceBlur;
    			second = speed < 5 ? null : speed < 25 ? diceClear : null;
    			if (second != null) {
    				firstAlpha = 1;
    				secondAlpha = (speed - 5f) / 20f;
    			} else {
    				firstAlpha = 1f;
    				secondAlpha = 0;
    			}
    		}

    		if (bmp == null) {
    			return;
    		}

    		alphaPaint.setAlpha((int) (firstAlpha * 255));
    		canvas.save();
    		canvas.rotate(polygon.degree, polygon.center.x, polygon.center.y);
    		canvas.drawBitmap(bmp, polygon.center.x - bmp.getWidth() / 2f, polygon.center.y - bmp.getHeight() / 2f, null);
    		if (second != null) {
    			secondPaint.setAlpha((int) (secondAlpha * 255));
    			canvas.drawBitmap(second, polygon.center.x - second.getWidth() / 2f, polygon.center.y - second.getHeight() / 2f, secondPaint);
    		}
    		canvas.restore();
    	}

    	public void chooseBitmap(Context context, int point) {
    		if (point == this.dicePoint) {
    			return;
    		}
    		this.dicePoint = point;
    		diceBitmap = bitmapCache.get(point);
    		if (diceBitmap != null) {
    		    return;
    		}

    		int id = context.getResources().getIdentifier("dice0" + point, "drawable", context.getPackageName());
    		Bitmap bmp = id == 0 ? null : BitmapFactory.decodeResource(context.getResources(), id);
    		bitmapCache.put(point, bmp);
    		diceBitmap = bmp;
    	}
    }

    public static interface AnimationCallback {
        void onAnimationFinished();
    }

    public void setAnimationCallback(AnimationCallback callback) {
        mCallback = callback;
    }

    private void finishAnimation() {
        if (mCallback != null) {
            mCallback.onAnimationFinished();
        }
        displayMotion.startMotion(polygon1, polygon2, 500);
        invalidateSelf();
    }
}
