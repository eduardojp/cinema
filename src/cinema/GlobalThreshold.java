package br.com.domm.ledblind.desktop.segmentation;

/**
 * Binariza a imagem definindo automaticamente o limiar de binarizacao.
 * 
 */
public class GlobalThreshold {

	/**
	 * Binariza a imagem definindo o valor 0xFF000000 para o texto e 0xFFFFFFFF para o fundo da imagem.
	 * 
	 * @param pixels pixels da imagem
	 * @param histogram vetor buffer para calculo de histograma
	 * @param threshold a estimativa inicial do limiar
	 * @param width largura da imagem
	 * @param height altura da imagem
	 */
	public static void run(int[] pixels, int width, int height, int threshold) {
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				int x_y = x + y*width;

				if(pixels[x_y] == 0)
					continue;

				if((pixels[x_y] & 0xFF) < threshold)
					pixels[x_y] = 0xFF000000;
				else
					pixels[x_y] = 0xFFFFFFFF;
			}
		}
	}

	/**
	 * Implementa de forma automatica a binarizacao global apresentada no livro de Gonzalez.
	 * </p>
	 * O algoritmo realizada a binarizacao de forma iterativa a partir de uma estimativa inicial para o limiar,
	 * calculada pela media dos pixels da imagem. A cada iteracao, calcula-se a media de pixels nas duas metades
	 * do histograma (de 0 ate o limiar e do limiar ate 255) e um novo limiar é definido como a soma entre essas
	 * médias dividada por 2. O processo é repetido até que a o limiar calculado seja igual ao limiar anterior.
	 * 
	 * @param pixels matriz de pixels que conterao o resultado da binarizacao (0xFF000000 ou 0xFFFFFFFF)
	 * @param histogram vetor buffer para calculo de histograma
	 * @param width largura da imagem
	 * @param height altura da imagem
	 */
	public static void runAutomatic(int[] pixels, int[] histogram, int width, int height) {
		int x, y;
		int total = 0;
		int old_threshold = -1;
		int lum, x_y;
		int sum1, sum2;
		int background, textColor;
		int threshold = 0;
		double m1, m2;

		for(x = 0; x < 256; x++) {
			histogram[x] = 0;
		}

		for(x = 0; x < width; x++) {
			for(y = 0; y < height; y++) {
				lum = pixels[x + y*width] & 0xFF;

				threshold += lum;
				histogram[lum]++;
				total++;
			}
		}
		
		threshold = (int)((double)threshold / (double)total + 0.5);

		while(old_threshold != threshold) {
			old_threshold = threshold;

			sum1 = 0;
			total = 0;
			for(x = 0; x < threshold; x++) {
				sum1 += histogram[x]*x;
				total += histogram[x];
			}
			m1 = (double)sum1 / (double)total;

			sum2 = 0;
			total = 0;
			for(x = 255; x >= threshold; x--) {
				sum2 += histogram[x]*x;
				total += histogram[x];
			}
			m2 = (double)sum2 / (double)total;

			threshold = (int)((m1+m2)/2 + 0.5);
		}

		//threshold += 20;

		sum1 = sum2 = 0;
		for(int h = 0; h < histogram.length; h++) {
			if(histogram[h] < threshold)
				sum1 += histogram[h];
			else
				sum2 += histogram[h];
		}

		background = 0xFFFFFFFF;
		textColor = 0xFF000000;

		for(x = 0; x < width; x++) {
			for(y = 0; y < height; y++) {
				x_y = x + y*width;

				if((pixels[x_y] & 0xFF) < threshold)
					pixels[x_y] = textColor;
				else
					pixels[x_y] = background;
			}
		}
	}
	
	public static void invert(int[] pixels, int width, int height) {
		int x, y, x_y;
		int totalWhite = 0;
		int totalBlack = 0;
		
		for(y = 0; y < height; y++) {
			for(x = 0; x < width; x++) {
				x_y = x + y*width;
				
				if(x != 1 || y != 1 || x != width-2 || y != height-2)
					continue;
				
				if(pixels[x_y] == 0xFFFFFFFF) totalWhite++;
				if(pixels[x_y] == 0xFF000000) totalBlack++;
			}
		}
		
		if(totalWhite > totalBlack)
			return;
		
		for(y = 0; y < height; y++) {
			for(x = 0; x < width; x++) {
				x_y = x + y*width;
				
				if(x != 1 || y != 1 || x != width-2 || y != height-2)
					continue;
				
				if(pixels[x_y] == 0xFFFFFFFF) totalWhite++;
				if(pixels[x_y] == 0xFF000000) totalBlack++;
			}
		}
	}
}
