var ADBE = {};

var Doc = {
	ADBE : ADBE,
	external: true,
	getField : function(name) {
		return JSDoc.getField(name);
	},
        getFieldByRef : function(ref) {
                return JSDoc.getFieldByRef(ref);
        }
};

function Event(type, src, target) {
    this.source = src;
    this.target = target;
    this.type = type;
    this.change = "";
    this.changeEX = "";
    this.commitKey = 0;
    this.name = "";
    this.rc = true;
    this.richChange = [];
    this.richChangeEx = [];
    this.richValue = [];
    this.targetName = "";
    this.value = null;
    this.willCommit = false;
    
    switch(type) {
            // Page Events
        case 31 :
            this.name = "Open";
            this.type = "Page";
        break;
        case 19 :
            this.name = "Close";
            this.type = "Page";
	break;
	// Field Events
	case 27 :
            this.name = "Keystroke";
            this.type = "Field";
	break;
	case 38 :
            this.name = "Validate";
            this.type = "Field";
	break;
	case 4866 :
            this.name = "Calculate";
            this.type = "Field";
	break;
	case 22 :
            this.name = "Format";
            this.type = "Field";
	break;
        default :
        break;
    }
}