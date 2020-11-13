package dev.dialector.glottony.ast

interface Node

interface Reference<T : Node>

interface FileContent : Node

class File(val contents: List<FileContent>) : Node {

}

class StructDeclaration(val name: String /* TODO: Add more */) : Node

class FunctionDeclaration(val name: String) : Node {

}

class ParameterList(val parameters: List<Parameter>) {

}

class Parameter(val name: String, val type: Type) {

}

interface Type : Node

class IntegerType : Type

class NumberType : Type

class StringType : Type

class StructType(val ofStruct: Reference<StructDeclaration>) : Type
