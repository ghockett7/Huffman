import java.util.PriorityQueue;

public class HuffProcessor implements Processor {

			String[] valArray;
	
	@Override
	public void compress(BitInputStream in, BitOutputStream out) {

		int[] characterArray = new int[ALPH_SIZE];
		int bitsread = in.readBits(BITS_PER_WORD);
		
		while (bitsread != -1){
			characterArray[bitsread]++;
			bitsread = in.readBits(BITS_PER_WORD);
		}

		in.reset();
		// u can use either an array that has 256 characters or a map. 
	//create huffman tree
		
		PriorityQueue<HuffNode> PQH = new PriorityQueue<HuffNode>();
		for (int i=0;i<ALPH_SIZE;i++){
			if (characterArray[i]!=0){
				PQH.add(new HuffNode(i,characterArray[i]));
			}
		}
		System.out.println(PQH.size());
		PQH.add(new HuffNode(PSEUDO_EOF,0));
		
		while (PQH.size() > 1){
			HuffNode HNode1 = PQH.poll();
			HuffNode HNode2 = PQH.poll();
			HuffNode comboNode = new HuffNode(-1,HNode1.weight()+HNode2.weight(),HNode1,HNode2);
			PQH.add(comboNode);
			//poll two smallest nodes
			//combine them into a new HuffNode
				//use other constructor
				//set right and left subtrees equal to the ones you just pulled out
		}

		
		HuffNode root = PQH.poll();
		
		
		valArray = new String[ALPH_SIZE+1];
		extractCodes(root,"");
		
		
		out.writeBits(BITS_PER_INT, HUFF_NUMBER);
		//must be set before recursion
		writeHeader(root,out);
	
		
		int bitsread1 = in.readBits(BITS_PER_WORD);
		while(bitsread1!=-1){
			String code = valArray[bitsread1];
			if (code!=null){
				out.writeBits(code.length(), Integer.parseInt(code,2));
			}
			bitsread1 = in.readBits(BITS_PER_WORD);
		}
		
		String pseudoCode = valArray[PSEUDO_EOF];
		if (pseudoCode!=null){
			out.writeBits(pseudoCode.length(), Integer.parseInt(pseudoCode,2));
		}
	}
	
	private void extractCodes(HuffNode current, String path){
		//extract the codes by traversing the tree
		if(current!=null){
		if(current.left()==null&&current.right()==null){
			valArray[current.value()] = path;
			return;
		}
		}
		extractCodes(current.left(),path+"0");
		extractCodes(current.right(),path+"1");
	}	
	
	private void writeHeader(HuffNode current, BitOutputStream out){
		//write header of the file
		
		if(current!=null && current.left()==null&&current.right()==null){
			
			out.writeBits(1, 1);
			out.writeBits(9, current.value());
		}
		
		else {
			
		
			out.writeBits(1,0);
			writeHeader(current.left(),out);
			writeHeader(current.right(),out);
		}
	}

	@Override
	public void decompress(BitInputStream in, BitOutputStream out) {
		//1. Check for huff Number
		if(in.readBits(32)!=HUFF_NUMBER){
			throw new HuffException("Not compressible, first 32 bits are not HUFF_NUMBER");
		}
		//parse body of compressed file
		HuffNode root = readHeader(in);
		HuffNode current = root;
		int bitsread = in.readBits(1);
		while(bitsread!=-1){
			if(bitsread==-1){
				throw new HuffException("Problem with PSEUDO_EOF");
			}
			if(bitsread==1 && current.right()!=null){
				current = current.right();	
			}
			else if(bitsread==0 && current.left()!=null){
				current = current.left();
			}
			if(current.left()==null&&current.right()==null){
				if (current.value()==PSEUDO_EOF){
					return;
				}
				else{
					out.writeBits(8, current.value());
					current=root;
				}
			}
			bitsread = in.readBits(1);
			
		}
		
	}
		//recreate tree from header
		private HuffNode readHeader(BitInputStream in){
			int bitsread = in.readBits(1);
			if(bitsread==0){
				HuffNode left = readHeader(in);
				HuffNode right = readHeader(in);
				HuffNode combinedNode = new HuffNode(-1,0,left,right);
				return combinedNode;
				
				
			}
			else{
				return new HuffNode(in.readBits(9),0);
			}
			
		}
		
		
		
		
	

}