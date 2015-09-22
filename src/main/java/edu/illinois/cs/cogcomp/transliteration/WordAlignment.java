using System;
using System.Collections.Generic;
using System.Text;

namespace SPTransliteration
{
    [Serializable]
    internal struct WordAlignment
    {
        /// <summary>
        /// Each string had exactly one word.
        /// </summary>
        public int oneToOne;

        /// <summary>
        /// There was an equal number of more than one words in each string.
        /// </summary>
        public int equalNumber;

        /// <summary>
        /// There were more words in one string than the other.
        /// </summary>
        public int unequalNumber;

        public WordAlignment(int oneToOne, int equalNumber, int unequalNumber)
        {
            this.oneToOne = oneToOne;
            this.equalNumber = equalNumber;
            this.unequalNumber = unequalNumber;
        }

        public override string ToString()
        {
            return oneToOne + ":" + equalNumber + ":" + unequalNumber;
        }

        public WordAlignment(string wordAlignmentString)
        {
            string[] values = wordAlignmentString.Split(':');
            oneToOne = int.Parse(values[0]);
            equalNumber = int.Parse(values[1]);
            unequalNumber = int.Parse(values[2]);
        }

        public override bool Equals(object obj)
        {
            if (obj is WordAlignment)
                return (this == (WordAlignment)obj);
            else
                return false;
        }

        public override int GetHashCode()
        {
            return oneToOne ^ (equalNumber << 10) ^ (equalNumber >> 22) ^ (unequalNumber << 21) ^ (unequalNumber >> 11);
        }

        public static WordAlignment operator + (WordAlignment wa1, WordAlignment wa2)
        {
            return new WordAlignment(wa1.oneToOne + wa2.oneToOne, wa1.equalNumber + wa2.equalNumber, wa1.unequalNumber + wa2.unequalNumber);
        }

        public static WordAlignment operator -(WordAlignment wa1, WordAlignment wa2)
        {
            return new WordAlignment(wa1.oneToOne - wa2.oneToOne, wa1.equalNumber - wa2.equalNumber, wa1.unequalNumber - wa2.unequalNumber);
        }

        public static bool operator ==(WordAlignment wa1, WordAlignment wa2)
        {
            return wa1.oneToOne == wa2.oneToOne && wa1.equalNumber == wa2.equalNumber && wa1.unequalNumber == wa2.unequalNumber;
        }

        public static bool operator !=(WordAlignment wa1, WordAlignment wa2)
        {
            return !(wa1==wa2);
        }

        public static bool operator > (WordAlignment wa1, WordAlignment wa2)
        {
            if (wa1.oneToOne == wa2.oneToOne)
            {
                if (wa1.equalNumber == wa2.equalNumber)
                    return (wa1.unequalNumber > wa2.unequalNumber);
                else
                    return wa1.equalNumber > wa2.equalNumber;
            }
            else
                return wa1.oneToOne > wa2.oneToOne;                      
        }

        public static bool operator <(WordAlignment wa1, WordAlignment wa2)
        {
            if (wa1.oneToOne == wa2.oneToOne)
            {
                if (wa1.equalNumber == wa2.equalNumber)
                    return (wa1.unequalNumber < wa2.unequalNumber);
                else
                    return wa1.equalNumber < wa2.equalNumber;
            }
            else
                return wa1.oneToOne < wa2.oneToOne;
        }

        public static bool operator >=(WordAlignment wa1, WordAlignment wa2)
        {
            if (wa1.oneToOne == wa2.oneToOne)
            {
                if (wa1.equalNumber == wa2.equalNumber)
                    return (wa1.unequalNumber >= wa2.unequalNumber);
                else
                    return wa1.equalNumber >= wa2.equalNumber;
            }
            else
                return wa1.oneToOne >= wa2.oneToOne;
        }

        public static bool operator <=(WordAlignment wa1, WordAlignment wa2)
        {
            if (wa1.oneToOne == wa2.oneToOne)
            {
                if (wa1.equalNumber == wa2.equalNumber)
                    return (wa1.unequalNumber <= wa2.unequalNumber);
                else
                    return wa1.equalNumber <= wa2.equalNumber;
            }
            else
                return wa1.oneToOne <= wa2.oneToOne;
        }
    }
}
