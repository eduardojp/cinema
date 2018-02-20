package cinema;

public class RegionGrowing {
    public static final int WHITE = 0xFFFFFFFF;

    public static int getWordsJava(int[] pixels, int[] map, int[] queue, int width, int height) {
        int x, y;
        int x0, x1, y0, y1;
        int rH = 3;
        int rV = 1;

        int begin, end, px, py;
        int w, h, w0, h0, w1, h1, i;
        int id = 0;

        // Pega as linhas
        for(y = 0; y < height; y++) {
            for(x = 0; x < width; x++) {
                if(map[x + y*width] != WHITE)
                    continue;

                begin = 0;
                end = 1;
                queue[0] = x + y*width;
                map[x + y*width] = id;

                x0 = x;
                x1 = x;
                y0 = y;
                y1 = y;

                // Region growing
                while(begin != end) {
                    px = queue[begin] % width;
                    py = queue[begin] / width;
                    begin++;

                    w0 = (px-rH < 0) ? 0 : px-rH;
                    h0 = (py-rV < 0) ? 0 : py-rV;
                    w1 = (px+rH > width-1) ? width-1 : px+rH;
                    h1 = (py+rV > height-1) ? height-1 : py+rV;

                    for(h = h0; h <= h1; h++) {
                        for(w = w0; w <= w1; w++) {
                            if(map[w + h*width] != WHITE)
                                continue;

                            queue[end] = w + h*width;
                            map[w + h*width] = id;
                            end++;

                            if(w < x0) x0 = w;
                            if(w > x1) x1 = w;
                            if(h < y0) y0 = h;
                            if(h > y1) y1 = h;
                        }
                    }
                }

                //Word word = Word.createWord(map, width, height, x0, y0, x1, y1, id);

                if(end > 1) {
                    id++;
                }
                else {
//                    for(i = 0; i < end; i++) {
//                        w = queue[i] % width;
//                        h = queue[i] / width;
//                        map[w + h*width] = SMALL_REGION;
//                    }
                }
            }
        }
        
        return id;
    }
}
