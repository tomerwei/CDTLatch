# encoding=utf-8

import itertools

from adt.tree.transform import TreeTransform
from adt.graph.format import DigraphFormatter
from adt.graph.transform.apply import ApplyTo

from pattern.collection.strings import MultiSubstitution
from pattern.optimize.cache import OncePerInstance
from pattern.meta.event_driven import EventBox

from logic.fol.syntax.parser import FolFormulaParser, LexerRules
from logic.fol import Identifier, FolFormula
from logic.smt.smtlib.sexpr import SExpression
from logic.smt.smtlib.output import SmtLib2OutputFormat, NamingConvention
from logic.fol.semantics.graphs import AlgebraicStructureGraphTool
from ui.text.lists import bulleted_list
from ui.text.table import side_by_side
from ui.text import indent_block
from filesystem.paths import CommonPath
from logic.fol.syntax.transform.delta import DeltaReduction
from logic.smt.smtlib.input import SmtLib2InputFormat
from logic.fol.syntax.transform import AuxTransformers
from logic.fol.semantics.extensions.sorts import FolManySortSignature, FolSorts
from logic.fol.semantics.extensions.arith import FolIntegerArithmetic
from synopsis.declare import TypeDeclarations


try:
    from synopsis.richtext import FormattedText
except ImportError:
    FormattedText = None # Qt not available

try:
    from adt.graph.visual.graphviz import GraphvizGraphLayout
except ImportError:
    GraphvizGraphLayout = None # pygraphviz not available



class SynopsisFormulaParser(FolFormulaParser):
    class LexerRules(FolFormulaParser.LexerRules):
        def t_IDENTIFIER(self, t):
            r"""[a-zA-Z0-9_#\']+['"*+#_]*[0-9]*"""
            return super(SynopsisFormulaParser.LexerRules, self).t_IDENTIFIER(t)
    class ParserRules(FolFormulaParser.ParserRules):
        LexerRules = LexerRules



    

class Expansion(object):
    
    class FormulaReader(object):
        
        def __init__(self):
            self.defops = [(":=", 2, LexerRules.opAssoc.RIGHT)]
            if ':=' not in FolFormula.INFIXES:
                FolFormula.INFIXES += [":="]
            
        @OncePerInstance
        def _mkparser(self):
            return SynopsisFormulaParser(operators=FolFormulaParser.OPERATORS+self.defops)
        
        @property
        def parser(self):
            return self._mkparser()
        
        def __call__(self, program_text):
            if isinstance(program_text, (str, unicode)):
                inputs = self._separate_blocks(program_text)
            else:
                inputs = program_text
    
            for line in inputs:
                phi = line if isinstance(line, FolFormula) else \
                      self.parser(line)
                yield phi

        def _separate_blocks(self, program_text):
            buf = ""
            for line in program_text.splitlines(True):
                if line.startswith('#'): continue
                indented = any(line.startswith(ws) for ws in " \t")
                if not indented:
                    if buf.strip(): yield buf
                    buf = ''
                buf += line
            if buf.strip(): yield buf

    def __init__(self):
        self.reader = self.FormulaReader()
        self.delta = DeltaReduction(recurse=True)
        self.fwd_defs = []
    
    @property
    def parser(self):
        return self.reader.parser

    def __call__(self, program_text):
        defs = self.delta.transformers
        
        for phi in self.reader(program_text):
            try:
                xt = DeltaReduction.Transformer(self.delta, phi)
                # @@@ OH YUCK! forward 'pvars' so it gets all the way to
                #  the proof synopsis
                if xt.head in self.fwd_defs: 
                    yield phi; continue
                defs += [xt]
            except ValueError:
                # not a definition
                psi = self.delta(phi)
                yield psi
                
            


from functools import partial



class SynopsisNamingConvention(NamingConvention):
    
    ESCAPE_SEQ = {"'": "@", '"': "@@", '#': "@1@"}
    VALID_IDENTIFIER_RE = r'[_a-zA-Z0-9*$@=>]+$' 
    
    def __init__(self):
        import re
        m = self.ESCAPE_SEQ
        ck = re.compile(self.VALID_IDENTIFIER_RE)
        self.escape = Identifier.lift(MultiSubstitution(m))
        self.unescape = Identifier.lift(MultiSubstitution({v:k for k,v in m.iteritems()}))
        self._is_valid_identifier = Identifier.lift(partial(self._check, ck))
        
    def check(self, name):
        if isinstance(name, Identifier) and name.kind in ['connective', 'quantifier']:
            return
        self._is_valid_identifier(name)
        
        




class ProofSynopsis(object):
    
    def __init__(self):
        self.expansion = e = Expansion()
        
        macros = ['lemma(phi) := [;]([](push), echo(["], phi, ["]), assert(not(phi)), []([check-sat]), []([get-model]), [](pop))',
                   "check := lemma(false)"]
        macros_f = self.macros_f = DeltaReduction()
        macros_f.transformers += \
            [DeltaReduction.Transformer(macros_f, e.parser(m)) for m in macros]

        self.alpha_renaming_f = AuxTransformers.renumber_bound_vars

        self.type_declarations = TypeDeclarations()

        self.smt = SmtLib2InputFormat(naming_convention=SynopsisNamingConvention())
        self.libs = []
        
        self.on_assertion = EventBox()

    def emit_form(self, expr):
        expr = self.macros_f(expr)
        r = expr.root
        if r == ';':
            return [SExpression.reconstruct(t) for t in expr.split(';')]
        else:
            return [SExpression('assert', [expr])]

    def emit(self, outfile, se):
        parts = []
        xform = TreeTransform([partial(self.obligations, collect=parts)], dir=TreeTransform.BOTTOM_UP)
        parts += [xform(se)]
        for part in parts:
            for cmd in self.emit_form(part):
                print >>outfile, cmd

    def emit_preface(self, outfile):
        print >>outfile, self.smt.preface
        for decl in self.smt.to_declare(self.type_declarations):
            print '###', decl
            print >>outfile, decl

    def obligations(self, t, collect=[]):
        if t.root == 'valid':
            collect += [type(t)('lemma', [x]) for x in t.subtrees]
            return type(t)(True)

    def __call__(self, program_text):
        import os.path, tempfile
        outfilename = os.path.join(tempfile.gettempdir(), "synopsis.smt2")
        outfile = open(outfilename, 'w')
    
        e = self.expansion
    
        stream = itertools.imap(self.alpha_renaming_f,
                     itertools.chain(*[e(x) for x in self.libs + [program_text]]))
    
        # First pass to collect all declarations
        def first_pass():
            for phi in stream:
                if self.type_declarations.is_declaration(phi):
                    self.type_declarations.read_from([phi])
                else:
                    self.on_assertion(phi, e)
                    yield phi  # fwd to 2nd pass
            self.emit_preface(outfile)
        
        stream = list(first_pass())
                    
        # Second pass gets the rest and emits them as assertions
        for phi in stream:
            y = side_by_side([" *>", bulleted_list(phi.split(), bullet='')], colsep='')
            print unicode(y)
    
            for se in self.smt.to_sexprs(phi):
                self.emit(outfile, se)
                
        return outfilename



class TCSpecific(object):
    
    @classmethod
    def restore_tc_from_rtc(cls, m, relation_name, tc_relation_name=None, rtc_relation_name=None):
        nstar = m.interpretation[rtc_relation_name]  # n*
        nplus_restored = lambda u,v: nstar(u,v) and u != v
        m.interpretation[tc_relation_name] = nplus_restored
    
    @classmethod
    def restore_edge_relation_from_tc(cls, m, relation_name, tc_relation_name=None):
        if tc_relation_name is None:
            t, r = cls._locate_tc_and_rtc(m, relation_name)
            if t not in m.interpretation:
                cls.restore_tc_from_rtc(m, relation_name, t, r)
            tc_relation_name = t
        nplus = m.interpretation[tc_relation_name]  # n+
        n_restored = lambda u,v: nplus(u,v) and not \
                                 any(nplus(u,w) and nplus(w,v)
                                     for w in m.domain)
        m.interpretation[relation_name] = n_restored

    @classmethod
    def _locate_tc_and_rtc(cls, m, relation_name, fallback=None):
        for i in xrange(len(relation_name)+1):
            t = relation_name[:i] + '+' + relation_name[i:]
            r = relation_name[:i] + '*' + relation_name[i:]
            if t in m.interpretation or r in m.interpretation:
                break
        else:
            if fallback is None:
                raise KeyError, u"tc of %s" % relation_name
            
        return t, r


class DummyStopwatch(object):
    def __enter__(self):            return self
    def __exit__(self, exc, v, tb): pass



def run_z3_and_produce_model(smtlib2_filename, gui=True, swatch=DummyStopwatch()):
    
    if gui and (FormattedText is None or GraphvizGraphLayout is None):
        print "warning: Qt/pygraphviz packages missing, GUI disabled."
        gui = False
    
    import os.path, tempfile
    outfn = os.path.join(tempfile.gettempdir(), "model")
    z3_exe = CommonPath().find_file('z3' if os.name == 'posix' else 'z3.exe')
    with swatch:
        os.system("%s %s > %s" % (z3_exe, smtlib2_filename, outfn))
        z3_model = open(outfn).read()
    
    ofmt = SmtLib2OutputFormat()
    ofmt.naming = SynopsisNamingConvention()
    d = ofmt(z3_model)
    
    # Filter out some undesired "model is not available" messages
    d_ = [x for x in d if not 
          (isinstance(x, d.MessageItem) and 'model is not available' in x.msg)]
    
    doc_cells = []
    
    for e in d_:
        if isinstance(e, d.ModelItem):
            m = e.structure
            toc = []
            #viz = False
            for n in ['n', "n'", 'n"', '_n', 'p', '_p', '__n', 'n0', 'n1', 'n2', 'n3', 'n4', 'n5', 'n12', 'R', 'p2', 'p3']:
                try:
                    TCSpecific.restore_edge_relation_from_tc(m, n)
                except KeyError:
                    continue
                toc += [n]
                _, nstar = TCSpecific._locate_tc_and_rtc(m, n)
                print m.binary_relation_as_table(nstar)
                
            #for r in ["R", "F2", "F3"]:
            #    if r in m.interpretation:
            #        print m.binary_relation_as_table(r)
            # add data fields here for printing in output:
            # for example: 
            #toc += [r for r in ["R", "F2", "F3", "A", "B", "D", "data"] if r in m.interpretation]
            toc += [r for r in ["R", "F2", "F3", "A", "B", "D", "data"] if r in m.interpretation]

            for c in ['b0', 'h', 'e', 't']:
                if c in m.interpretation:
                    doc_cells += [('txt', c + " = " + unicode(m.interpretation[c]))]
                    
            for up in ['C']:
                if up in m.interpretation:
                    cf = m.interpretation[up]
                    s = [x for x in m.domain if cf(x)]
                    doc_cells += [('txt', up + " = " + unicode(s))]

            for relation in toc:
                g = AlgebraicStructureGraphTool(m, [relation])()
                g = ApplyTo(nodes=lambda x: x.replace("V!val!", ':')).inplace(g)
                doc_cells += [('txt', '%s : %s' % (relation, "V -> V -> bool"))]
                if gui:
                    fn = GraphvizGraphLayout().with_(output_format='svg')(g)
                    doc_cells += [('img', open(fn, "rb").read())]
                else:
                    fmtd = DigraphFormatter()(g)
                    doc_cells += [('txt', indent_block(fmtd))]
                #viz = True
            if not toc:
                doc_cells += [('txt', "(no relations to draw)")]
        else:
            if FormattedText:
                text = FormattedText(e)
                if isinstance(e, d.MessageItem):
                    text.css['color'] = 'grey'
            else:
                text = unicode(e)
            doc_cells += [('txt', text)]

    return doc_cells



def show_results(doc_cells):
    print "#cells: ", len(doc_cells)

    if any(t for t,_ in doc_cells if t == 'img'):
        from synopsis.richtext import RichTextApp
        a = RichTextApp([])
        for cell_type, cell_content in doc_cells:
            if cell_type == 'img':
                a.put_image(cell_content)
            else:
                a.put_text_block(cell_content)
        a()
    else:
        print '=' * 60
        print "  RESULTS"
        print '=' * 60
        for cell in doc_cells:
            print cell[1]
    



if __name__ == '__main__':
    import sys
    if len(sys.argv) > 1:
        fol_fn = sys.argv[1]
    else:
        fol_fn = "../examples/compose/sll-compose-reverse-filter.fol"
    
    inputs = open(fol_fn).read()

    import os.path
    here = os.path.dirname(os.path.realpath(__file__))
    
    preface = open(os.path.join(here, "../list-preface.smt2")).read()
    sll_lib = open(os.path.join(here, "../sll.fol")).read()

    syn = ProofSynopsis()
    syn.smt.preface = preface
    syn.libs += [sll_lib]
    
    ofn = syn(inputs)
    
  
    run_z3_and_produce_model(ofn)
