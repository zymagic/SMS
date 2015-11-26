package test.blur;

public class Step {

    int radius;

    int index = 0;
    int remain = 0;
    int r = 0;
    int dx = 0;
    int dy = 0;

    public Step(int radius) {
        this.radius = radius;
    }

    public void next(int[] out) {
        if (index >= remain) {
            r++;
            remain = 8 * r;
            index = 0;
            out[0] = r - 1;
            out[1] = -r + 1;
        } else {
            int a = index % (2 * r);
            int b = index / (2 * r);
            switch (b) {
                case 0:
                    out[0] = r;
                    out[1] = -r + 1 + a;
                    break;
                case 1:
                    out[0] = r - a - 1;
                    out[1] = r;
                    break;
                case 2:
                    out[0] = -r;
                    out[1] = r - a - 1;
                    break;
                case 3:
                    out[0] = -r + 1 + a;
                    out[1] = -r;
                    break;
            }
            index++;
        }
    }

    public void reset() {
        index = 0;
    }
}
