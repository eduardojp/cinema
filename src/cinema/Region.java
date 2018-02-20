package br.com.domm.ledblind.desktop.segmentation;

import java.util.Collections;
import java.util.LinkedList;

public class Region implements Comparable<Region> {
	public static final int X = 0;
	public static final int Y = 1;
	
	public int nPixels = 0;
	public int id, x0, y0, x1, y1;
	public int[] leftPoint = new int[2];
	public int[] rightPoint = new int[2];
	public int[] upperPoint = new int[2];
	public int[] bottomPoint = new int[2];
	public double[] center = new double[2];
	public int seed;
	public LinkedList<int[]> frontier;
	public LinkedList<int[]> newFrontier;
	public Region left, right;
	
	public Region(int id) {
		this.frontier = new LinkedList<int[]>();
		this.id = id;
		this.x0 = Integer.MAX_VALUE;
		this.y0 = Integer.MAX_VALUE;
		this.x1 = Integer.MIN_VALUE;
		this.y1 = Integer.MIN_VALUE;
		this.center[X] = 0;
		this.center[Y] = 0;
		this.left = null;
		this.right = null;
	}
	
	public Region(int id, int seed, int leftPoint, int rightPoint, int upperPoint, int bottomPoint, int width) {
		this.id = id;
		this.seed = seed;
		this.x0 = Integer.MAX_VALUE;
		this.y0 = Integer.MAX_VALUE;
		this.x1 = Integer.MIN_VALUE;
		this.y1 = Integer.MIN_VALUE;
		this.center[X] = 0;
		this.center[Y] = 0;
		this.left = null;
		this.right = null;

		this.leftPoint[0] = leftPoint % width;
		this.leftPoint[1] = leftPoint / width;
		
		this.rightPoint[0] = rightPoint % width;
		this.rightPoint[1] = rightPoint / width;
		
		this.upperPoint[0] = upperPoint % width;
		this.upperPoint[1] = upperPoint / width;
		
		this.bottomPoint[0] = bottomPoint % width;
		this.bottomPoint[1] = bottomPoint / width;
		
		this.x0 = this.leftPoint[0];
		this.y0 = this.upperPoint[1];
		
		this.x1 = this.rightPoint[0];
		this.y1 = this.bottomPoint[1];
		
		center[X] = (x1 + x0) / 2.0;
		center[Y] = (y1 + y0) / 2.0;
	}
	
	public void add(int x, int y) {
		this.frontier.add(new int[] {x, y});
		
		if(x < x0) x0 = x;
		if(y < y0) y0 = y;
		if(x > x1) x1 = x;
		if(y > y1) y1 = y;
	}
	
	public void addNew(int x, int y) {
		this.newFrontier.add(new int[] {x, y});
		
		if(x < x0) x0 = x;
		if(y < y0) y0 = y;
		if(x > x1) x1 = x;
		if(y > y1) y1 = y;
		
		//Log.i("mopa", "add: " + x + " " + y);
	}
	
	public boolean expand(int[] pixels, int[] map, int width, int height) {
		int x, y;
		newFrontier = new LinkedList<int[]>();
	
		for(int[] p : frontier) {
			x = p[0];
			y = p[1];
			
			map[x + y*width] = id;
			
			if(x > 0 && map[(x-1) + y*width] == SpecialGrowing.EXP) {
				addNew(x-1, y);
				map[(x-1) + y*width] = id;
			}
			
			if(x < width-1 && map[(x+1) + y*width] == SpecialGrowing.EXP) {
				addNew(x+1, y);
				map[(x+1) + y*width] = id;
			}
			
			if(y > 0 && map[x + (y-1)*width] == SpecialGrowing.EXP) {
				addNew(x, y-1);
				map[x + (y-1)*width] = id;
			}
			
			if(y < height-1 && map[x + (y+1)*width] == SpecialGrowing.EXP) {
				addNew(x, y+1);
				map[x + (y+1)*width] = id;
			}
		}
		
		if(!newFrontier.isEmpty()) {
			frontier = newFrontier;
			return true;
		}
		else {
			return false;
		}
	}

	public int compareTo(Region r2) {
		if(this.x0 < r2.x0) {
			return -1;
		}
		else if(this.x0 > r2.x0) {
			return 1;
		}
		else {
			if(this.y0 < r2.y0) {
				return -1;
			}
			else if(this.y0 > r2.y0) {
				return 1;
			}
			else {
				return 0;
			}
		}
	}
	
	public static Region[] sort(LinkedList<Region> regions, int[] map, int[] oldMap) {
		int[] idSwapper = new int[regions.size()];
		int i;
		
		for(i = 0; i < map.length; i++) {
			oldMap[i] = map[i];
		}
		
		Collections.sort(regions);
		
		i = 0;
		Region[] regionsArray = new Region[regions.size()];
		for(Region r : regions) {
			idSwapper[r.id] = i;
			r.id = i;
			regionsArray[i++] = r;
		}
		
		for(i = 0; i < map.length; i++) {
			//FIXME
			if(oldMap[i] != RegionGrowing.BACKGROUND && oldMap[i] >= 0) {
				map[i] = idSwapper[oldMap[i]];
			}
		}
		
		return regionsArray;
	}
}
