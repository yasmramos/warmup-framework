package io.warmup.framework.startup.hotpath;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for HotnessLevel enum.
 * Tests the hotness level enum values and utility methods.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
class HotnessLevelTest {
    
    @Test
    void testEnumValues() {
        HotnessLevel[] expectedValues = {
            HotnessLevel.COLD,
            HotnessLevel.LUKEWARM,
            HotnessLevel.WARM,
            HotnessLevel.HOT,
            HotnessLevel.CRITICAL,
            HotnessLevel.VERY_HOT,
            HotnessLevel.EXTREMELY_HOT
        };
        
        HotnessLevel[] actualValues = HotnessLevel.values();
        assertEquals(expectedValues.length, actualValues.length);
        
        for (HotnessLevel expected : expectedValues) {
            assertTrue(Arrays.asList(actualValues).contains(expected));
        }
    }
    
    @Test
    void testColdLevel() {
        assertEquals(0, HotnessLevel.COLD.getLevel());
        assertEquals("Cold Path", HotnessLevel.COLD.getDescription());
        assertFalse(HotnessLevel.COLD.isHot());
        assertFalse(HotnessLevel.COLD.isCritical());
    }
    
    @Test
    void testLukewarmLevel() {
        assertEquals(1, HotnessLevel.LUKEWARM.getLevel());
        assertEquals("Lukewarm Path", HotnessLevel.LUKEWARM.getDescription());
        assertFalse(HotnessLevel.LUKEWARM.isHot());
        assertFalse(HotnessLevel.LUKEWARM.isCritical());
    }
    
    @Test
    void testWarmLevel() {
        assertEquals(2, HotnessLevel.WARM.getLevel());
        assertEquals("Warm Path", HotnessLevel.WARM.getDescription());
        assertFalse(HotnessLevel.WARM.isHot());
        assertFalse(HotnessLevel.WARM.isCritical());
    }
    
    @Test
    void testHotLevel() {
        assertEquals(3, HotnessLevel.HOT.getLevel());
        assertEquals("Hot Path", HotnessLevel.HOT.getDescription());
        assertTrue(HotnessLevel.HOT.isHot());
        assertFalse(HotnessLevel.HOT.isCritical());
    }
    
    @Test
    void testCriticalLevel() {
        assertEquals(4, HotnessLevel.CRITICAL.getLevel());
        assertEquals("Critical Path", HotnessLevel.CRITICAL.getDescription());
        assertTrue(HotnessLevel.CRITICAL.isHot());
        assertTrue(HotnessLevel.CRITICAL.isCritical());
    }
    
    @Test
    void testVeryHotLevel() {
        assertEquals(5, HotnessLevel.VERY_HOT.getLevel());
        assertEquals("Very Hot Path", HotnessLevel.VERY_HOT.getDescription());
        assertTrue(HotnessLevel.VERY_HOT.isHot());
        assertTrue(HotnessLevel.VERY_HOT.isCritical());
    }
    
    @Test
    void testExtremelyHotLevel() {
        assertEquals(6, HotnessLevel.EXTREMELY_HOT.getLevel());
        assertEquals("Extremely Hot Path", HotnessLevel.EXTREMELY_HOT.getDescription());
        assertTrue(HotnessLevel.EXTREMELY_HOT.isHot());
        assertTrue(HotnessLevel.EXTREMELY_HOT.isCritical());
    }
    
    @Test
    void testIsHotMethod() {
        // Non-hot levels
        assertFalse(HotnessLevel.COLD.isHot());
        assertFalse(HotnessLevel.LUKEWARM.isHot());
        assertFalse(HotnessLevel.WARM.isHot());
        
        // Hot levels
        assertTrue(HotnessLevel.HOT.isHot());
        assertTrue(HotnessLevel.CRITICAL.isHot());
        assertTrue(HotnessLevel.VERY_HOT.isHot());
        assertTrue(HotnessLevel.EXTREMELY_HOT.isHot());
    }
    
    @Test
    void testIsCriticalMethod() {
        // Non-critical levels
        assertFalse(HotnessLevel.COLD.isCritical());
        assertFalse(HotnessLevel.LUKEWARM.isCritical());
        assertFalse(HotnessLevel.WARM.isCritical());
        assertFalse(HotnessLevel.HOT.isCritical());
        
        // Critical levels
        assertTrue(HotnessLevel.CRITICAL.isCritical());
        assertTrue(HotnessLevel.VERY_HOT.isCritical());
        assertTrue(HotnessLevel.EXTREMELY_HOT.isCritical());
    }
    
    @Test
    void testGetLevel() {
        assertEquals(0, HotnessLevel.COLD.getLevel());
        assertEquals(1, HotnessLevel.LUKEWARM.getLevel());
        assertEquals(2, HotnessLevel.WARM.getLevel());
        assertEquals(3, HotnessLevel.HOT.getLevel());
        assertEquals(4, HotnessLevel.CRITICAL.getLevel());
        assertEquals(5, HotnessLevel.VERY_HOT.getLevel());
        assertEquals(6, HotnessLevel.EXTREMELY_HOT.getLevel());
    }
    
    @Test
    void testGetDescription() {
        assertEquals("Cold Path", HotnessLevel.COLD.getDescription());
        assertEquals("Lukewarm Path", HotnessLevel.LUKEWARM.getDescription());
        assertEquals("Warm Path", HotnessLevel.WARM.getDescription());
        assertEquals("Hot Path", HotnessLevel.HOT.getDescription());
        assertEquals("Critical Path", HotnessLevel.CRITICAL.getDescription());
        assertEquals("Very Hot Path", HotnessLevel.VERY_HOT.getDescription());
        assertEquals("Extremely Hot Path", HotnessLevel.EXTREMELY_HOT.getDescription());
    }
    
    @Test
    void testValueOf() {
        assertEquals(HotnessLevel.COLD, HotnessLevel.valueOf("COLD"));
        assertEquals(HotnessLevel.LUKEWARM, HotnessLevel.valueOf("LUKEWARM"));
        assertEquals(HotnessLevel.WARM, HotnessLevel.valueOf("WARM"));
        assertEquals(HotnessLevel.HOT, HotnessLevel.valueOf("HOT"));
        assertEquals(HotnessLevel.CRITICAL, HotnessLevel.valueOf("CRITICAL"));
        assertEquals(HotnessLevel.VERY_HOT, HotnessLevel.valueOf("VERY_HOT"));
        assertEquals(HotnessLevel.EXTREMELY_HOT, HotnessLevel.valueOf("EXTREMELY_HOT"));
    }
    
    @Test
    void testLevelOrdering() {
        // Verify that levels are properly ordered
        assertTrue(HotnessLevel.COLD.getLevel() < HotnessLevel.LUKEWARM.getLevel());
        assertTrue(HotnessLevel.LUKEWARM.getLevel() < HotnessLevel.WARM.getLevel());
        assertTrue(HotnessLevel.WARM.getLevel() < HotnessLevel.HOT.getLevel());
        assertTrue(HotnessLevel.HOT.getLevel() < HotnessLevel.CRITICAL.getLevel());
        assertTrue(HotnessLevel.CRITICAL.getLevel() < HotnessLevel.VERY_HOT.getLevel());
        assertTrue(HotnessLevel.VERY_HOT.getLevel() < HotnessLevel.EXTREMELY_HOT.getLevel());
        
        // Verify consecutive levels
        assertEquals(1, HotnessLevel.LUKEWARM.getLevel() - HotnessLevel.COLD.getLevel());
        assertEquals(1, HotnessLevel.WARM.getLevel() - HotnessLevel.LUKEWARM.getLevel());
        assertEquals(1, HotnessLevel.HOT.getLevel() - HotnessLevel.WARM.getLevel());
        assertEquals(1, HotnessLevel.CRITICAL.getLevel() - HotnessLevel.HOT.getLevel());
        assertEquals(1, HotnessLevel.VERY_HOT.getLevel() - HotnessLevel.CRITICAL.getLevel());
        assertEquals(1, HotnessLevel.EXTREMELY_HOT.getLevel() - HotnessLevel.VERY_HOT.getLevel());
    }
    
    @Test
    void testHotnessProgression() {
        // Test that each level represents increasing hotness
        List<HotnessLevel> levelsInOrder = Arrays.asList(
            HotnessLevel.COLD,
            HotnessLevel.LUKEWARM,
            HotnessLevel.WARM,
            HotnessLevel.HOT,
            HotnessLevel.CRITICAL,
            HotnessLevel.VERY_HOT,
            HotnessLevel.EXTREMELY_HOT
        );
        
        for (int i = 0; i < levelsInOrder.size() - 1; i++) {
            HotnessLevel current = levelsInOrder.get(i);
            HotnessLevel next = levelsInOrder.get(i + 1);
            
            assertTrue(current.getLevel() < next.getLevel(), 
                current + " should be less hot than " + next);
        }
    }
    
    @Test
    void testHotVsCriticalSeparation() {
        // Verify the separation between hot and critical levels
        assertEquals(3, HotnessLevel.HOT.getLevel());
        assertEquals(4, HotnessLevel.CRITICAL.getLevel());
        
        // All levels 0-3 should not be critical
        assertFalse(HotnessLevel.COLD.isCritical());
        assertFalse(HotnessLevel.LUKEWARM.isCritical());
        assertFalse(HotnessLevel.WARM.isCritical());
        assertFalse(HotnessLevel.HOT.isCritical());
        
        // Levels 4-6 should be critical
        assertTrue(HotnessLevel.CRITICAL.isCritical());
        assertTrue(HotnessLevel.VERY_HOT.isCritical());
        assertTrue(HotnessLevel.EXTREMELY_HOT.isCritical());
    }
}