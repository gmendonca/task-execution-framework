import java.io.PrintWriter;


public class WorkloadGenerator {
	
	public static void main(String[] args){
		int count = 500;
		
		PrintWriter writer;
		try {
			writer = new PrintWriter("workload2", "UTF-8");
			for(int i = 0; i < count; i ++)
				writer.println("sleep 0");
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Done");
	}

}
