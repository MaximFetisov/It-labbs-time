import java.util.Random;

public class labprog12 {
    public static void main(String[] args)
    {
    int[] w = new int[9];
    for (int i = 0; i < 9; i++){
        w[i] = 6 + i*2;
        }

    float[] x = new float[10];
    Random rand = new Random();
    for (int i = 0; i < 10; i++){
        x[i] = rand.nextFloat() *9.0f - 7.0f;
    }

    double[][] s = new double[9][10];
    for (int i = 0; i < 9; i ++){
        for (int j = 0; j < 10;j++){
            s[i][j] = elem(w[i],x[j],i);
        }
    }
    
    pm(s);



    }
    public static double elem(int wi, float xi,int i){
        if (wi == 20){
            double cosX = Math.cos(xi);
            double inner = cosX*(cosX - Math.PI);
            return Math.tan(Math.pow(inner,3));
        }
        else if (wi == 8 || wi == 14 || wi == 22 || wi == 18 ){
            double sinX = Math.sin(xi);
            return Math.cos(((2.0/3.0)/(4 - sinX)));
        }
        else {
            double expX = Math.exp(xi);
            double x2 = 3.0/4.0 + Math.log(Math.abs(xi));
            double x3 = Math.pow(0.5 * xi,3);
            double x4 = 4 * Math.pow(expX,x3 / x2)+0.5;
            return Math.cos(Math.pow(x4,2));
        }
    }




    public static void pm(double[][] m) {
        for (int i = 0; i < m.length;i++){
             for (int j = 0; j < m.length;j++){
                System.out.printf("%.3f ", m[i][j]);
            }
            System.out.println();
        }
    }
}
