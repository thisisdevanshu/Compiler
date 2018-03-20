package cop5556sp18;

import cop5556sp18.AST.Declaration;

import java.util.*;

public class SymbolTable {

    Stack<Integer> scopeStack = new Stack<>();
    Map<String, List<Value>> map = new HashMap<>();
    int currentScope;
    int nextScope;

    public void enterScope(){
        currentScope = nextScope++;
        scopeStack.push(currentScope);
    }

    public void leaveScope(){
        scopeStack.pop();
        currentScope = scopeStack.peek();
    }

    public void insert(String ident, Declaration declaration){
        List<Value> values = new ArrayList<>();
        Value val = new Value(currentScope, declaration);
        if(map.containsKey(ident)){
            values = map.get(ident);
            for(Value value : values){
                if( value.scope == currentScope ){
                    return ;
                }
            }
        }
        values.add(val);
        map.put(ident, values);
    }

    public Declaration lookup(String ident){
        if(!map.containsKey(ident)){
            return null;
        }
        Declaration declaration=null;
        List<Value> values = map.get(ident);
        for(int i = values.size()-1 ; i >= 0 ; i--){
            int temp_scope = values.get(i).scope;
            if(scopeStack.contains(temp_scope)){
                declaration = values.get(i).declaration;
                break;
            }
        }
        return declaration;
    }

    public int getScope(String ident){
        if(!map.containsKey(ident)){
            return -1;
        }
        List<Value> values = map.get(ident);

        for(Value value : values){
            if(value.scope == currentScope){
                return currentScope;
            }
        }
        return -1;
    }

    public SymbolTable(){
        this.currentScope = 0;
        this.nextScope = 1;
        scopeStack.push(0);
    }

    public class Value{
        public int scope;
        public Declaration declaration;
        public Value(int scope, Declaration declaration){
            this.scope = scope;
            this.declaration = declaration;
        }
    }

}
