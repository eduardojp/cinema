package br.com.domm.ledblind.desktop.segmentation;

import java.util.Collections;
import java.util.LinkedList;

import br.com.domm.ledblind.desktop.utils.ColorSpace;



public class SpecialGrowing {
	public static final int LETTER = -1;
	public static final int EXP = -2;
	public static final int BACKGROUND = -3;
	
	public static void runWithBloodCuts(int[] pixels, int[] map, int width, int height, int[] queueX, int[] queueY) {
		int x, y, w, h;
		int begin, end, px, py;
		int[] temp = new int[width * height];
		int color;
		
		//Binarizacao lógica da imagem
		for(y = 0; y < height; y++) {
			for(x = 0; x < width; x++) {
				if((pixels[x + y*width] & 0xFF) < 128)
					pixels[x + y*width] = LETTER;
				else
					pixels[x + y*width] = BACKGROUND;
			}
		}
		
		// Pega as linhas
		for(y = 0; y < height; y++) {
			for(x = 0; x < width; x++) {
				if(pixels[x + y*width] != LETTER)
					continue;

				color = ColorSpace.nextColor();

				begin = 0;
				end = 1;
				queueX[0] = x;
				queueY[0] = y;
				pixels[x + y*width] = color;

				// Region growing
				while(begin != end) {
					px = (int)queueX[begin];
					py = (int)queueY[begin];
					
					begin++;
					
					if(px > 0 && pixels[(px-1) + py*width] == LETTER) {
						queueX[end] = (px-1);
						queueY[end] = py;
						pixels[(px-1) + py*width] = color;
						end++;
					}
					if(px < width-1 && pixels[(px+1) + py*width] == LETTER) {
						queueX[end] = (px+1);
						queueY[end] = py;
						pixels[(px+1) + py*width] = color;
						end++;
					}
					if(py > 0 && pixels[px + (py-1)*width] == LETTER) {
						queueX[end] = px;
						queueY[end] = py-1;
						pixels[px + (py-1)*width] = color;
						end++;
					}
					if(py < height-1 && pixels[px + (py+1)*width] == LETTER) {
						queueX[end] = px;
						queueY[end] = py+1;
						pixels[px + (py+1)*width] = color;
						end++;
					}
				}
			}
		}
		
		for(y = 0; y < height; y++) {
			for(x = 0; x < width; x++) {
				if(map[x + y*width] == 0xFFFF0000) {
					pixels[x + y*width] = LETTER;
				}
			}
		}
	}
	
	public static Region[] runCompetitive(int[] pixels, int[] map, int[] queue, int width, int height) {
		int x, y, x_y, w, h, x0, y0, x1, y1, i;
		int begin, end, px, py;
		int id = 0;
		int total;
		LinkedList<Region> regions = new LinkedList<Region>();
		Region region;
		
		//Binarizacao lógica da imagem
		for(y = 0; y < height; y++) {
			for(x = 0; x < width; x++) {
				x_y = x + y*width;
				
				if(pixels[x_y] != 0xFFFFFFFF) {
					if((pixels[x_y] & 0xFF) < 128 && map[x_y] != 0xFFFF0000) {
						map[x_y] = LETTER;
					}
					else {
						map[x_y] = EXP;
					}
				}
				else {
					map[x_y] = BACKGROUND;
				}
//				if(pixels[x_y] != 0xFFFFFFFF) {
//					if((pixels[x_y] & 0xFF) < 128) {
//						map[x_y] = LETTER;
//					}
//					else {
//						map[x_y] = EXP;
//					}
//				}
//				else {
//					map[x_y] = BACKGROUND;
//				}
			}
		}
		
		// Pega as linhas
		for(x = 0; x < width; x++) {
			for(y = 0; y < height; y++) {
				if(map[x + y*width] != LETTER)
					continue;

				region = new Region(id);
				begin = 0;
				end = 1;
				queue[0] = x + y*width;
				map[x + y*width] = id;
				total = 0;

				//Region growing
				while(begin != end) {
					px = queue[begin] % width;
					py = queue[begin] / height;
					
					begin++;
					total += 255 - (pixels[px + py*width] & 0xFF);
					
					if(px > 0) {
						if(map[(px-1) + py*width] == LETTER) {
							queue[end] = (px-1) + py*width;
							map[(px-1) + py*width] = id;
							end++;
						}
						else if(map[(px-1) + py*width] == EXP) {
							region.add(px-1, py);
						}
					}
					
					if(px < width-1) {
						if(map[(px+1) + py*width] == LETTER) {
							queue[end] = (px+1) + py*width;
							map[(px+1) + py*width] = id;
							end++;
						}
						else if(map[(px+1) + py*width] == EXP) {
							region.add(px+1, py);
						}
					}
					
					if(py > 0) {
						if(map[px + (py-1)*width] == LETTER) {
							queue[end] = px + (py-1)*width;
							map[px + (py-1)*width] = id;
							end++;
						}
						else if(map[px + (py-1)*width] == EXP) {
							region.add(px, py-1);
						}
					}
					
					if(py < height-1) {
						if(map[px + (py+1)*width] == LETTER) {
							queue[end] = px + (py+1)*width;
							map[px + (py+1)*width] = id;
							end++;
						}
						else if(map[px + (py+1)*width] == EXP) {
							region.add(px, py+1);
						}
					}
				}
				
				if(total > 10*255) {
					regions.add(region);
					id++;
				}
				else {
					for(i = 0; i < end; i++) {
						w = queue[i] % width;
						h = queue[i] / width;
						map[w + h*width] = EXP;
					}
				}
			}
		}
		
		boolean exp = true;
		
		//Expansao das fronteiras
		while(exp) {
			exp = false;
			
			for(Region r : regions) {
				exp |= r.expand(pixels, map, width, height);
			}
		}
		
		//Pinta as fronteiras
//		for(y = 0; y < height; y++) {
//			for(x = 0; x < width; x++) {
//				if(x == 0 || map[x + y*width] != map[(x-1) + y*width]) {
//					pixels[x + y*width] = 0xFFFF0000;
//				}
//				if(x == width-1 || map[x + y*width] != map[(x+1) + y*width]) {
//					pixels[x + y*width] = 0xFFFF0000;
//				}
//				if(y == 0 || map[x + y*width] != map[x + (y-1)*width]) {
//					pixels[x + y*width] = 0xFFFF0000;
//				}
//				if(y == height-1 || map[x + y*width] != map[x + (y+1)*width]) {
//					pixels[x + y*width] = 0xFFFF0000;
//				}
//				
//				if(map[x + y*width] == EXP) {
//					pixels[x + y*width] = 0xFF00FFFF;
//				}
//			}
//		}
		
		return Region.sort(regions, map, queue);
	}
	
	public static LinkedList<Integer> getWordLimits(int[] mapRegions, int[] mapWords, int[] queue, int width, int height) {
		int x, y, x_y;
		int begin, end, px, py;
		int id = 0;
		LinkedList<Integer> wordLimits = new LinkedList<Integer>();
		int max;
		
		//Binarizacao lógica da imagem
		for(y = 0; y < height; y++) {
			for(x = 0; x < width; x++) {
				x_y = x + y*width;
				
				if(mapRegions[x_y] != BACKGROUND) {
					mapWords[x_y] = LETTER;
				}
				else {
					mapWords[x_y] = BACKGROUND;
				}
			}
		}
		
		// Pega as linhas
		for(x = 0; x < width; x++) {
			for(y = 0; y < height; y++) {
				if(mapWords[x + y*width] != LETTER)
					continue;

				max = 0;
				begin = 0;
				end = 1;
				queue[0] = x + y*width;
				mapWords[x + y*width] = id;

				//Region growing
				while(begin != end) {
					px = queue[begin] % width;
					py = queue[begin] / width;
					
					begin++;
					
					if(mapRegions[px + py*width] > max) {
						max = mapRegions[px + py*width];
					}
					
					if(px > 0) {
						if(mapWords[(px-1) + py*width] == LETTER) {
							queue[end] = (px-1) + py*width;
							mapWords[(px-1) + py*width] = id;
							end++;
						}
					}
					
					if(px < width-1) {
						if(mapWords[(px+1) + py*width] == LETTER) {
							queue[end] = (px+1) + py*width;
							mapWords[(px+1) + py*width] = id;
							end++;
						}
					}
					
					if(py > 0) {
						if(mapWords[px + (py-1)*width] == LETTER) {
							queue[end] = px + (py-1)*width;
							mapWords[px + (py-1)*width] = id;
							end++;
						}
					}
					
					if(py < height-1) {
						if(mapWords[px + (py+1)*width] == LETTER) {
							queue[end] = px + (py+1)*width;
							mapWords[px + (py+1)*width] = id;
							end++;
						}
					}
				}
				
				wordLimits.add(max+1);
			}
		}
		
		Collections.sort(wordLimits);
		
		return wordLimits;
	}
}
