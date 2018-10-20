/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.transliteration;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.utils.TopList;

import java.util.Iterator;
import java.util.Scanner;

/**
 * Created by mayhew2 on 5/30/16.
 */
public class Interactive {

    public static void main(String[] args) throws Exception {
        String modelname = args[0];

        interactive(modelname);
    }

    static void interactive(String modelname) throws Exception {
        SPModel model = new SPModel(modelname);

        //List<String> arabicStrings = Program.getForeignWords(wikidata + "wikidata.Armenian");
        //model.SetLanguageModel(arabicStrings);

        Scanner scanner = new Scanner(System.in);

        while(true){
            System.out.print("Enter something: ");
            String name = scanner.nextLine().toLowerCase();

            if(name.equals("exit")){
                break;
            }

            System.out.println(name);

            TopList<Double, String> cands = model.Generate(name);
            Iterator<Pair<Double,String>> ci = cands.iterator();

            int lim = Math.min(5, cands.size());

            if(lim == 0){
                System.out.println("No candidates for this...");
            }else {
                for (int i = 0; i < lim; i++) {
                    Pair<Double, String> p = ci.next();
                    System.out.println(p.getFirst() + ": " + p.getSecond());
                }
            }
        }
    }


}
