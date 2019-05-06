package genepi.mut.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import genepi.mut.objects.Sample;
import genepi.mut.objects.Variant;

public class FastaWriter {

	public void createFasta(String in, String out, String reference) {

		MutationServerReader reader = new MutationServerReader(in);
		double level = 0.5;

		try {
			
			String contents = ReferenceUtil.readInReference(reference);
			FileWriter writer = new FileWriter(new File(out));
			StringBuilder build = new StringBuilder();
			HashMap<String, Sample> samples = reader.parse();

			for (Sample sam : samples.values()) {
				
				build.setLength(0);
				
				build.append(">" + sam.getId() + "\n");

				int i = 0;

				for (String ref : contents.split("")) {
					i++;
					ArrayList<Variant> vars = sam.getVariants(i);
					
					// write reference if no variant found
					if (vars == null) {
						build.append(ref);
						continue;
					}
					

					int type1 = 0;
					int type5 = 0;
					boolean complex = false;
					boolean multiInsertion = false;

					for (Variant var : vars) {

						if ((var.getType() >= 1 && var.getType() <= 4) && var.getLevel() > level) {
							type1++;
						}
						if (var.getType() == 5) {
							type5++;
						}
					}
					if (type1 >= 1 && type5 >= 1) {
						complex = true;
					} else if (type5 > 1) {
						multiInsertion = true;
					}
					
					if (!complex && !multiInsertion) {

							// since nothing complex, there should only be one entry for this position
							Variant var = vars.get(0);

							// deletions
							if (var.getVariant() == 'D' && var.getLevel() > level) {
								System.out.println(var.getPos());
								continue;
							}
							// write heteroplasmies greater than 50 %
							else if (var.getType() == 2 && var.getLevel() >= level) {
								build.append(var.getVariant());
							} else if ((var.getType() == 5)) {
								build.append(ref + "" + var.getVariant());
							} else if ((var.getType() == 1)) {
								build.append(var.getVariant());
							} else {
								build.append(ref);
							}
					} else {

						if (multiInsertion && !complex) {
							StringBuilder insertionBuilder = new StringBuilder();

							for (Variant var : vars) {
								if (var.getType() == 5) {
									insertionBuilder.append(var.getVariant());
								}
							}
							String insertion = insertionBuilder.toString();
							build.append(ref + insertion);
						}

						if (complex) {

							StringBuilder complexBuilder = new StringBuilder();
							String variant = ref;
							for (Variant var : vars) {
								if (var.getType() == 5) {
									complexBuilder.append(var.getVariant());
								} else if (var.getType() == 1
										|| (var.getType() == 2 && var.getLevel() >= level && var.getVariant() != 'D')) {
									variant = String.valueOf(var.getVariant());
								}
							}
							String insertion = complexBuilder.toString();
							build.append(variant + insertion);
						}

					}
				}
				writer.write(build.toString() + "\n");
			}
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
