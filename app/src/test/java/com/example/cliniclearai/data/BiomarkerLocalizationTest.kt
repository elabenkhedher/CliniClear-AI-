package com.example.cliniclearai.data

import com.example.cliniclearai.data.data_sources.local.BiomarkerLocalizationHelper
import com.example.cliniclearai.data.data_sources.local.OcrFallbackParser
import com.example.cliniclearai.domain.models.Biomarker
import com.example.cliniclearai.domain.models.BiomarkerStatus
import com.example.cliniclearai.domain.models.LabReport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BiomarkerLocalizationTest {

    @Test
    fun testBiomarkerLocalization_English() {
        val frenchBiomarker = Biomarker(
            id = "bio_1",
            name = "Hémoglobine",
            value = 14.5,
            unit = "g/dL",
            minReference = 12.0,
            maxReference = 17.0,
            status = BiomarkerStatus.NORMAL,
            vulgarization = "Protéine des globules rouges chargée de transporter l'oxygène dans tout le corps.",
            recommendation = "Consommer des aliments riches en fer et vitamine C pour favoriser l'oxygénation tissulaire."
        )

        val localizedEn = BiomarkerLocalizationHelper.localizeBiomarker(frenchBiomarker, "en")
        assertEquals("Hemoglobin", localizedEn.name)
        assertTrue(localizedEn.vulgarization.contains("oxygen"))
        assertTrue(localizedEn.recommendation.contains("iron"))
    }

    @Test
    fun testBiomarkerLocalization_Arabic() {
        val frenchBiomarker = Biomarker(
            id = "bio_1",
            name = "Hémoglobine",
            value = 14.5,
            unit = "g/dL",
            minReference = 12.0,
            maxReference = 17.0,
            status = BiomarkerStatus.NORMAL,
            vulgarization = "Protéine des globules rouges chargée de transporter l'oxygène dans tout le corps.",
            recommendation = "Consommer des aliments riches en fer et vitamine C pour favoriser l'oxygénation tissulaire."
        )

        val localizedAr = BiomarkerLocalizationHelper.localizeBiomarker(frenchBiomarker, "ar")
        assertEquals("الهيموغلوبين", localizedAr.name)
        assertTrue(localizedAr.vulgarization.contains("الأكسجين"))
        assertTrue(localizedAr.recommendation.contains("الحديد"))
    }

    @Test
    fun testReportLocalization_FullPipeline() {
        val sampleReport = LabReport(
            id = "rep_1",
            title = "Bilan Hématologique & NFS",
            date = "2026-11-23",
            laboratory = "Laboratoire Pasteur",
            biomarkers = listOf(
                Biomarker(
                    id = "b1",
                    name = "Glycémie à jeun",
                    value = 1.25,
                    unit = "g/L",
                    minReference = 0.70,
                    maxReference = 1.05,
                    status = BiomarkerStatus.HIGH,
                    vulgarization = "Taux de sucre circulant dans le sang après le jeûne de la nuit. Dépiste le diabète.",
                    recommendation = "Privilégier les fibres, sucres complexes et une activité physique régulière."
                )
            ),
            doctorSummary = "Bilan comprenant 1 paramètres analysés. 1 biomarqueur(s) se situent en dehors des valeurs de référence du laboratoire et nécessitent un avis médical.",
            hasAlerts = true
        )

        // Test En
        val enReport = BiomarkerLocalizationHelper.localizeReport(sampleReport, "en")
        assertEquals("Hematology & CBC Panel", enReport.title)
        assertEquals("Fasting Blood Glucose", enReport.biomarkers.first().name)
        assertTrue(enReport.biomarkers.first().vulgarization.contains("sugar"))
        assertTrue(enReport.doctorSummary.contains("Report containing 1 analyzed parameters"))

        // Test Ar
        val arReport = BiomarkerLocalizationHelper.localizeReport(sampleReport, "ar")
        assertEquals("تحليل الدم الشامل (CBC)", arReport.title)
        assertEquals("سكر الدم الصائم", arReport.biomarkers.first().name)
        assertTrue(arReport.biomarkers.first().vulgarization.contains("السكر"))
        assertTrue(arReport.doctorSummary.contains("يتضمن التقرير تحليل 1"))
    }

    @Test
    fun testOcrFallbackParser_Multilingual() {
        val sampleOcrText = """
            Laboratoire Pasteur
            Date: 23/11/2026
            Hémoglobine: 14.5 g/dL (12.0 - 17.0)
            Créatinine: 85.0 µmol/L (60.0 - 110.0)
        """.trimIndent()

        // Test parsing with English
        val enReport = OcrFallbackParser.parse(sampleOcrText, language = "en")
        assertTrue(enReport.biomarkers.isNotEmpty())
        val hbEn = enReport.biomarkers.find { it.name == "Hemoglobin" }
        assertNotNull(hbEn)
        assertTrue(hbEn!!.vulgarization.contains("oxygen"))

        // Test parsing with Arabic
        val arReport = OcrFallbackParser.parse(sampleOcrText, language = "ar")
        assertTrue(arReport.biomarkers.isNotEmpty())
        val hbAr = arReport.biomarkers.find { it.name == "الهيموغلوبين" }
        assertNotNull(hbAr)
        assertTrue(hbAr!!.vulgarization.contains("الأكسجين"))
    }
}
