package jp.ac.nii.ednii;

/**
 * Implements different string similarity measures used by the Islam model.
 * 
 * @author nltngan
 *
 */
public class StringSimilarity {
	
	public static double getIslamStringSimilarity(String S1, String S2, double alpha1, double alpha2, double alpha3, double alpha4){
		int len1 = S1.length();
		int len2 = S2.length();
		double v1 = 2 * getLCS(S1, S2).length()/(double)(len1 + len2);
		double v2 = 2 * getMCLCS1(S1, S2).length()/(double)(len1 + len2);
		double v3 = 2 * getMCLCSn(S1, S2).length()/(double)(len1 + len2);
		double v4 = 2 * getMCLCSz(S1, S2).length()/(double)(len1 + len2);
		return alpha1*v1 + alpha2*v2 + alpha3*v3 + alpha4*v4;
		
	}
	
	public static String getLCS(String a, String b){	
		int[][] lengths = new int[a.length() + 1][b.length() + 1];

		// row 0 and column 0 are initialized to 0 already

		for (int i = 0; i < a.length(); i++)
			for (int j = 0; j < b.length(); j++)
				if (a.charAt(i) == b.charAt(j))
					lengths[i + 1][j + 1] = lengths[i][j] + 1;
				else
					lengths[i + 1][j + 1] = Math.max(lengths[i + 1][j],
							lengths[i][j + 1]);

		// read the substring out from the matrix
		StringBuffer sb = new StringBuffer();
		for (int x = a.length(), y = b.length(); x != 0 && y != 0;) {
			if (lengths[x][y] == lengths[x - 1][y])
				x--;
			else if (lengths[x][y] == lengths[x][y - 1])
				y--;
			else {
				assert a.charAt(x - 1) == b.charAt(y - 1);
				sb.append(a.charAt(x - 1));
				x--;
				y--;
			}
		}

		return sb.reverse().toString();

	}
	
	private static String getMCLCSn(String S1, String S2)
	{
	    int Start = 0;
	    int Max = 0;
	    for (int i = 0; i < S1.length(); i++)
	    {
	        for (int j = 0; j < S2.length(); j++)
	        {
	            int x = 0;
	            while (S1.charAt(i + x) == S2.charAt(j + x))
	            {
	                x++;
	                if (((i + x) >= S1.length()) || ((j + x) >= S2.length())) break;
	            }
	            if (x > Max)
	            {
	                Max = x;
	                Start = i;
	            }
	         }
	    }
	    return S1.substring(Start, (Start + Max));
	}
	
	public static String getMCLCS1(String S1, String S2){
		
		int Max = 0;
		String tmp1, tmp2;
		if (S1.length() <= S2.length()){
			tmp1 = S1;
			tmp2 = S2;
		}else{
			tmp1 = S2;
			tmp2 = S1;
		}
		
		for (int i = 0; i < tmp1.length(); i++) {
			if (tmp1.charAt(i) == tmp2.charAt(i))
				Max++;
		}

		return tmp1.substring(0, Max);
	}
	
	public static String getMCLCSz(String S1, String S2){
		
		int Max = 0;
		String tmp1, tmp2;
		if (S1.length() <= S2.length()){
			tmp1 = S1;
			tmp2 = S2;
		}else{
			tmp1 = S2;
			tmp2 = S1;
		}
		
		for (int i = tmp1.length() - 1; i >= 0; i--) {
			if (tmp1.charAt(i) == tmp2.charAt(i))
				Max++;
		}

		return tmp1.substring(tmp1.length() - Max);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		System.out.println(StringSimilarity.getLCS("albastru", "alabasteru"));
		System.out.println(StringSimilarity.getLCS("alabasteru", "albastru"));
		System.out.println(StringSimilarity.getMCLCSn("albastru", "alabasteru"));
		System.out.println(StringSimilarity.getMCLCSn("alabasteru", "albastru"));
		System.out.println(StringSimilarity.getMCLCS1("albastru", "alabasteru"));
		System.out.println(StringSimilarity.getMCLCS1("alabasteru", "albastru"));
		System.out.println(StringSimilarity.getMCLCSz("albastru", "alabasteru"));
		System.out.println(StringSimilarity.getMCLCSz("alabasteru", "albastru"));
	}

}
