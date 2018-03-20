package cop5556sp18;

import cop5556sp18.AST.Declaration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class SymbolTable {

    int  currentScope, nextScope;
    Stack<Integer> scopeStack = new Stack<>();
    Map<String, ArrayList<Pair>> map = new HashMap <>();

    public void enterScope()
    {
        currentScope = nextScope++;
        scopeStack.push(currentScope);
    }

    public void leaveScope()
    {
        scopeStack.pop();
        currentScope = scopeStack.peek();
    }

    public boolean insert(String ident, Declaration declaration)
    {
        ArrayList<Pair> ps = new ArrayList<Pair>();
        Pair p = new Pair(currentScope, declaration);
        if(map.containsKey(ident))
        {
            ps = map.get(ident);
            for(Pair it: ps)
            {
                if(it.getScope()==currentScope)
                    return false;
            }
        }
        ps.add(p);
        map.put(ident, ps);
        return true;
    }

    public Declaration lookup(String ident)
    {
        if(!map.containsKey(ident))
            return null;

        Declaration declaration=null;
        ArrayList<Pair> ps = map.get(ident);
        for(int i=ps.size()-1;i>=0;i--)
        {
            int temp_scope = ps.get(i).getScope();
            if(scopeStack.contains(temp_scope))
            {
                declaration = ps.get(i).getDeclaration();
                break;
            }
        }
        return declaration;
    }

    public SymbolTable()
    {
        this.currentScope = 0;
        this.nextScope = 1;
        scopeStack.push(0);
    }

    public class Pair
    {
        int scope;
        Declaration declaration;
        public Pair(int s, Declaration d)
        {
            this.scope = s;
            this.declaration = d;
        }
        public int getScope()
        {
            return scope;
        }
        public Declaration getDeclaration()
        {
            return declaration;
        }
    }

}
