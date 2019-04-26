package genepi.mut.util;

import static org.junit.Assert.assertEquals;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import genepi.mut.objects.BasePosition;
import genepi.mut.objects.BayesFrequencies;
import genepi.mut.objects.VariantLine;

public class BayesTest {

	@Test
	public void BayesABaseNoFrequenciesTest() {

		InputStream in = this.getClass().getClassLoader().getResourceAsStream("1000g.frq");
		HashMap<String, Double> freq = BayesFrequencies.instance(new DataInputStream(in));

		ArrayList<Byte> g = new ArrayList<>();
		Byte e = new Byte("20");

		BasePosition basePos = new BasePosition();
		basePos.setPos(73);
		basePos.setaFor(10);

		for (int i = 0; i < basePos.getaFor(); i++) {
			g.add(e);
		}

		basePos.setaForQ(g);

		VariantLine line = new VariantLine();
		line.calcBayes(basePos, freq);

		assertEquals('A', line.getBayesBase());
		assertEquals(1.0, line.getBayesProbability(), 0.0);

	}

	@Test
	public void BayesCBaseNoFrequenciesTest() {

		InputStream in = this.getClass().getClassLoader().getResourceAsStream("1000g.frq");
		HashMap<String, Double> freq = BayesFrequencies.instance(new DataInputStream(in));

		ArrayList<Byte> g = new ArrayList<>();
		Byte e = new Byte("20");

		BasePosition basePos = new BasePosition();
		basePos.setPos(73);
		basePos.setcFor(10);

		for (int i = 0; i < basePos.getcFor(); i++) {
			g.add(e);
		}

		basePos.setcForQ(g);

		VariantLine line = new VariantLine();
		line.calcBayes(basePos, freq);

		assertEquals('C', line.getBayesBase());
		assertEquals(1.0, line.getBayesProbability(), 0.0);

	}

	@Test
	public void BayesGBaseNoFrequenciesTest() {

		InputStream in = this.getClass().getClassLoader().getResourceAsStream("1000g.frq");
		HashMap<String, Double> freq = BayesFrequencies.instance(new DataInputStream(in));

		ArrayList<Byte> g = new ArrayList<>();
		Byte e = new Byte("20");

		BasePosition basePos = new BasePosition();
		basePos.setPos(73);
		basePos.setgFor(10);

		for (int i = 0; i < basePos.getgFor(); i++) {
			g.add(e);
		}

		basePos.setgForQ(g);

		VariantLine line = new VariantLine();
		line.calcBayes(basePos, freq);

		assertEquals('G', line.getBayesBase());
		assertEquals(1.0, line.getBayesProbability(), 0.0);

	}

	@Test
	public void BayesTBaseNoFrequenciesTest() {

		InputStream in = this.getClass().getClassLoader().getResourceAsStream("1000g.frq");
		HashMap<String, Double> freq = BayesFrequencies.instance(new DataInputStream(in));

		ArrayList<Byte> g = new ArrayList<>();
		Byte e = new Byte("20");

		BasePosition basePos = new BasePosition();
		basePos.setPos(73);
		basePos.settFor(10);

		for (int i = 0; i < basePos.gettFor(); i++) {
			g.add(e);
		}

		basePos.settForQ(g);

		VariantLine line = new VariantLine();
		line.calcBayes(basePos, freq);

		assertEquals('T', line.getBayesBase());
		assertEquals(1.0, line.getBayesProbability(), 0.0);

	}

	@Test
	public void BayesTwoBaseFrequenciesTest() {

		InputStream in = this.getClass().getClassLoader().getResourceAsStream("1000g.frq");
		HashMap<String, Double> freq = BayesFrequencies.instance(new DataInputStream(in));

		ArrayList<Byte> g = new ArrayList<>();
		Byte e = new Byte("20");

		BasePosition basePos = new BasePosition();
		basePos.setPos(73);
		basePos.setaFor(200);
		basePos.setgFor(199);

		for (int i = 0; i < basePos.getaFor(); i++) {
			g.add(e);
		}

		basePos.setaForQ(g);

		g = new ArrayList<>();
		for (int i = 0; i < basePos.getgFor(); i++) {
			g.add(e);
		}

		basePos.setgForQ(g);

		VariantLine line = new VariantLine();
		line.calcBayes(basePos, freq);

		System.out.println(line.getBayesBase());
		System.out.println(line.getBayesProbability());
		
		assertEquals('A', line.getBayesBase());
		assertEquals(0.97757, line.getBayesProbability(), 0.001);
		System.out.println(line.getBayesBase());

	}

	@Test
	public void BayesTwoBaseFrequenciesDifferentQualitiesTest() {

		InputStream in = this.getClass().getClassLoader().getResourceAsStream("1000g.frq");
		HashMap<String, Double> freq = BayesFrequencies.instance(new DataInputStream(in));

		ArrayList<Byte> g = new ArrayList<>();
		Byte e1 = new Byte("20");
		Byte e = new Byte("30");
		
		BasePosition basePos = new BasePosition();
		basePos.setPos(73);
		basePos.setaFor(200);
		basePos.setgFor(199);

		for (int i = 0; i < basePos.getaFor(); i++) {
			g.add(e1);
		}

		basePos.setaForQ(g);

		g = new ArrayList<>();
		for (int i = 0; i < basePos.getgFor(); i++) {
			g.add(e);
		}

		basePos.setgForQ(g);

		VariantLine line = new VariantLine();
		line.calcBayes(basePos, freq);

		assertEquals('G', line.getBayesBase());
		assertEquals(1, line.getBayesProbability(), 0.001);
		System.out.println(line.getBayesBase());

	}

}
