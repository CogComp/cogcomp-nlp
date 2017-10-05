package edu.illinois.cs.cogcomp.transliteration;

import java.io.Serializable;

class WordAlignment implements Serializable {
    /// <summary>
    /// Each String had exactly one word.
    /// </summary>
    public int oneToOne;

    /// <summary>
    /// There was an equal number of more than one words in each String.
    /// </summary>
    public int equalNumber;

    /// <summary>
    /// There were more words in one String than the other.
    /// </summary>
    public int unequalNumber;

    public WordAlignment(int oneToOne, int equalNumber, int unequalNumber) {
        this.oneToOne = oneToOne;
        this.equalNumber = equalNumber;
        this.unequalNumber = unequalNumber;
    }

    @Override
    public String toString() {
        return oneToOne + ":" + equalNumber + ":" + unequalNumber;
    }

    public WordAlignment(String wordAlignmentString) {
        String[] values = wordAlignmentString.split(":");
        oneToOne = Integer.parseInt(values[0]);
        equalNumber = Integer.parseInt(values[1]);
        unequalNumber = Integer.parseInt(values[2]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WordAlignment that = (WordAlignment) o;

        if (oneToOne != that.oneToOne) return false;
        if (equalNumber != that.equalNumber) return false;
        return unequalNumber == that.unequalNumber;

    }

    @Override
    public int hashCode() {
        int result = oneToOne;
        result = 31 * result + equalNumber;
        result = 31 * result + unequalNumber;
        return result;
    }


    // FIXME: uh oh... java has no operator support.

//        public static WordAlignment operator + (WordAlignment wa1, WordAlignment wa2)
//        {
//            return new WordAlignment(wa1.oneToOne + wa2.oneToOne, wa1.equalNumber + wa2.equalNumber, wa1.unequalNumber + wa2.unequalNumber);
//        }
//
//        public static WordAlignment operator -(WordAlignment wa1, WordAlignment wa2)
//        {
//            return new WordAlignment(wa1.oneToOne - wa2.oneToOne, wa1.equalNumber - wa2.equalNumber, wa1.unequalNumber - wa2.unequalNumber);
//        }
//
//        public static Boolean operator ==(WordAlignment wa1, WordAlignment wa2)
//        {
//            return wa1.oneToOne == wa2.oneToOne && wa1.equalNumber == wa2.equalNumber && wa1.unequalNumber == wa2.unequalNumber;
//        }
//
//        public static bool operator !=(WordAlignment wa1, WordAlignment wa2)
//        {
//            return !(wa1==wa2);
//        }
//
//        public static bool operator > (WordAlignment wa1, WordAlignment wa2)
//        {
//            if (wa1.oneToOne == wa2.oneToOne)
//            {
//                if (wa1.equalNumber == wa2.equalNumber)
//                    return (wa1.unequalNumber > wa2.unequalNumber);
//                else
//                    return wa1.equalNumber > wa2.equalNumber;
//            }
//            else
//                return wa1.oneToOne > wa2.oneToOne;
//        }
//
//        public static bool operator <(WordAlignment wa1, WordAlignment wa2)
//        {
//            if (wa1.oneToOne == wa2.oneToOne)
//            {
//                if (wa1.equalNumber == wa2.equalNumber)
//                    return (wa1.unequalNumber < wa2.unequalNumber);
//                else
//                    return wa1.equalNumber < wa2.equalNumber;
//            }
//            else
//                return wa1.oneToOne < wa2.oneToOne;
//        }
//
//        public static bool operator >=(WordAlignment wa1, WordAlignment wa2)
//        {
//            if (wa1.oneToOne == wa2.oneToOne)
//            {
//                if (wa1.equalNumber == wa2.equalNumber)
//                    return (wa1.unequalNumber >= wa2.unequalNumber);
//                else
//                    return wa1.equalNumber >= wa2.equalNumber;
//            }
//            else
//                return wa1.oneToOne >= wa2.oneToOne;
//        }
//
//        public static bool operator <=(WordAlignment wa1, WordAlignment wa2)
//        {
//            if (wa1.oneToOne == wa2.oneToOne)
//            {
//                if (wa1.equalNumber == wa2.equalNumber)
//                    return (wa1.unequalNumber <= wa2.unequalNumber);
//                else
//                    return wa1.equalNumber <= wa2.equalNumber;
//            }
//            else
//                return wa1.oneToOne <= wa2.oneToOne;
//        }
}

