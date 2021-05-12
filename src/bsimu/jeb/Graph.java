package bsimu.jeb;
import java.util.*;
class Graph{

       private Integer adjMatrix[][];
       private int numVertices;

       // Initialize the matrix
       public Graph(int numVertices) {
           this.numVertices = numVertices;
           adjMatrix = new Integer[numVertices][numVertices];
           for(int i =0; i < numVertices; i++)
           {
               for(int j = 0; j < numVertices; j++)
               {
                    adjMatrix[i][j] = 0;
               }
           }
       }

       // Add edges
       public void addEdge(int i, int j) {
           adjMatrix[i][j] = 1;
           //adjMatrix[j][i] = true;
       }

       // Remove edges
       public void removeEdge(int i, int j) {
           adjMatrix[i][j] = 0;
           // adjMatrix[j][i] = false;
       }
       public ArrayList<Integer> top_sort()
       {
           int size = adjMatrix.length;
           int zero = 0;


           ArrayList<Integer> group_ids= new ArrayList<>(Arrays.asList(new Integer[size]));
           Collections.fill(group_ids, 0);

        //   Collections.fill(group_ids, zero);
           ArrayList<Integer> node_queue = new ArrayList<>();

           for(int i = 0; i < size; i++)
           {
               boolean is_root = true;
               for(int j =0; j < size; j++)
               {
                   if(adjMatrix[j][i] !=0)
                   {
                       is_root = false;
                       break;
                   }
               }
               if(is_root){
                   node_queue.add(i);
               }

           }
           if(node_queue.size()==0){
               System.out.println("no root found");

           }
           while  (node_queue.size() > 0)
           {
               int current_node = node_queue.get(node_queue.size() - 1);
               node_queue.remove(node_queue.size()-1);

               for(int i = 0; i < size; i++)
               {
                   if(adjMatrix[current_node][i] == 0)
                   {
                       continue;
                   }
                   if(group_ids.get(current_node) + 1 > group_ids.get(i))
                   {
                       group_ids.set(i, group_ids.get(current_node) + 1);
                       node_queue.add(i);
                   }
               }
           }
           return group_ids;

       }
}