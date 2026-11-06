package com.lipi.keyboard.lang

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OdiaTransliterationEngineTest {

    private lateinit var engine: OdiaTransliterationEngine

    @Before
    fun setUp() {
        val dict = setOf("ନମସ୍କାର", "ଧନ୍ୟବାଦ", "ଓଡ଼ିଶା", "ଆମେ", "ଆମି", "ଭଲ", "ସୁନ୍ଦର")
        engine = OdiaTransliterationEngine(dict)
    }

    @Test
    fun testCommonWords() {
        val namaskarCandidates = engine.processComposing("namaskar")
        assertTrue("Should contain ନମସ୍କାର", namaskarCandidates.contains("ନମସ୍କାର"))
        assertEquals("First candidate should be ନମସ୍କାର", "ନମସ୍କାର", namaskarCandidates.first())

        val odishaCandidates = engine.processComposing("odisha")
        assertTrue("Should contain ଓଡ଼ିଶା", odishaCandidates.contains("ଓଡ଼ିଶା"))
        assertEquals("First candidate should be ଓଡ଼ିଶା", "ଓଡ଼ିଶା", odishaCandidates.first())

        val dhanyabadCandidates = engine.processComposing("dhanyabad")
        assertTrue("Should contain ଧନ୍ୟବାଦ", dhanyabadCandidates.contains("ଧନ୍ୟବାଦ"))

        val amiCandidates = engine.processComposing("ami")
        assertTrue("Should contain ଆମି", amiCandidates.contains("ଆମି"))

        val jhotiCandidates = engine.processComposing("jhoti")
        assertTrue("Should contain ଝୋଟି", jhotiCandidates.contains("ଝୋଟି"))

        val bhubaneswar = engine.processComposing("bhubaneswar")
        assertTrue("Should contain ଭୁବନେଶ୍ୱର", bhubaneswar.contains("ଭୁବନେଶ୍ୱର"))

        val jagannath = engine.processComposing("jagannath").first()
        assertEquals("ଜଗନ୍ନାଥ", jagannath)

        val rasagola = engine.processComposing("rasagola").first()
        assertEquals("ରସଗୋଲା", rasagola)
    }

    @Test
    fun testIndependentVowels() {
        assertEquals("ଅ", engine.transliterate("a"))
        assertEquals("ଆ", engine.transliterate("aa"))
        assertEquals("ଇ", engine.transliterate("i"))
        assertEquals("ଈ", engine.transliterate("ee"))
        assertEquals("ଉ", engine.transliterate("u"))
        assertEquals("ଊ", engine.transliterate("oo"))
        assertEquals("ଋ", engine.transliterate("ru"))
        assertEquals("ଏ", engine.transliterate("e"))
        assertEquals("ଐ", engine.transliterate("ai"))
        assertEquals("ଓ", engine.transliterate("o"))
        assertEquals("ଔ", engine.transliterate("au"))
    }

    @Test
    fun testConsonantsAndInherentVowel() {
        assertEquals("କ", engine.transliterate("ka"))
        assertEquals("ଖ", engine.transliterate("kha"))
        assertEquals("ଗ", engine.transliterate("ga"))
        assertEquals("ଘ", engine.transliterate("gha"))
        assertEquals("ଚ", engine.transliterate("cha"))
        assertEquals("ଛ", engine.transliterate("chha"))
        assertEquals("ଜ", engine.transliterate("ja"))
        assertEquals("ଝ", engine.transliterate("jha"))
        assertEquals("ଟ", engine.transliterate("Ta"))
        assertEquals("ଠ", engine.transliterate("Tha"))
        assertEquals("ଡ", engine.transliterate("Da"))
        assertEquals("ଢ", engine.transliterate("Dha"))
        assertEquals("ଣ", engine.transliterate("Na"))
        assertEquals("ତ", engine.transliterate("ta"))
        assertEquals("ଥ", engine.transliterate("tha"))
        assertEquals("ଦ", engine.transliterate("da"))
        assertEquals("ଧ", engine.transliterate("dha"))
        assertEquals("ନ", engine.transliterate("na"))
        assertEquals("ପ", engine.transliterate("pa"))
        assertEquals("ଫ", engine.transliterate("pha"))
        assertEquals("ବ", engine.transliterate("ba"))
        assertEquals("ଭ", engine.transliterate("bha"))
        assertEquals("ମ", engine.transliterate("ma"))
        assertEquals("ୟ", engine.transliterate("ya"))
        assertEquals("ଯ", engine.transliterate("Ya"))
        assertEquals("ର", engine.transliterate("ra"))
        assertEquals("ଲ", engine.transliterate("la"))
        assertEquals("ସ", engine.transliterate("sa"))
        assertEquals("ହ", engine.transliterate("ha"))
    }

    @Test
    fun testMatras() {
        assertEquals("କା", engine.transliterate("kaa"))
        assertEquals("କି", engine.transliterate("ki"))
        assertEquals("କୀ", engine.transliterate("kee"))
        assertEquals("କୁ", engine.transliterate("ku"))
        assertEquals("କୂ", engine.transliterate("koo"))
        assertEquals("କୃ", engine.transliterate("kru"))
        assertEquals("କେ", engine.transliterate("ke"))
        assertEquals("କୈ", engine.transliterate("kai"))
        assertEquals("କୋ", engine.transliterate("ko"))
        assertEquals("କୌ", engine.transliterate("kau"))
    }

    @Test
    fun testConjuncts() {
        // consonant cluster should insert halant automatically
        assertEquals("ସ୍କ", engine.transliterate("sk"))
        assertEquals("କ୍ଷ", engine.transliterate("ksh"))
        assertEquals("ଜ୍ଞ", engine.transliterate("jny"))
        assertEquals("ପ୍ର", engine.transliterate("pr"))
        assertEquals("ସ୍ତ୍ର", engine.transliterate("str"))
    }

    @Test
    fun testNuktaAndModifiers() {
        assertEquals("ଡ଼", engine.transliterate("d."))
        assertEquals("ଢ଼", engine.transliterate("dh."))
        assertEquals("କଂ", engine.transliterate("kaM"))
        assertEquals("ଦୁଃଖ", engine.transliterate("duHkha"))
        assertEquals("ମାଁ", engine.transliterate("maa~"))
    }

    @Test
    fun testDigits() {
        assertEquals("୧୨୩୪୫", engine.transliterate("12345"))
        assertEquals("୬୭୮୯୦", engine.transliterate("67890"))
    }
}
