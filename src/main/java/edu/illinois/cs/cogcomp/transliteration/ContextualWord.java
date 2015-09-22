using System;
using System.Collections.Generic;
using System.Text;

namespace SPTransliteration
{
    struct Context
    {
        public Context(string leftContext, string rightContext)
        {
            this.leftContext = leftContext;
            this.rightContext = rightContext;
        }

        public string leftContext;
        public string rightContext;
    }
}
