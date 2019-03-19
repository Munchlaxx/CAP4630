
%%%%%%%%% Simple Prolog Planner %%%%%%%%%%%%%%%%%%%%%%%%%%
%%%
%%% Based on one of the sample programs in:
%%%
%%% Artificial Intelligence:
%%% Structures and strategies for complex problem solving
%%%
%%% by George F. Luger and William A. Stubblefield
%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
:- module( planner,
	   [
	       plan/4,change_state/3,conditions_met/2,member_state/2,
	       move/3,go/2,test1/0,test2/0
	   ]).

:- [utils].

plan(State, Goal, _, Moves) :-	equal_set(State, Goal),
				write('moves are'), nl,
				reverse_print_stack(Moves).
plan(State, Goal, Been_list, Moves) :-
				move(Name, Preconditions, Actions),
				conditions_met(Preconditions, State),
				change_state(State, Actions, Child_state),
				not(member_state(Child_state, Been_list)),
				stack(Child_state, Been_list, New_been_list),
				stack(Name, Moves, New_moves),
			plan(Child_state, Goal, New_been_list, New_moves),!.

change_state(S, [], S).
change_state(S, [add(P)|T], S_new) :-	change_state(S, T, S2),
					add_to_set(P, S2, S_new), !.
change_state(S, [del(P)|T], S_new) :-	change_state(S, T, S2),
					remove_from_set(P, S2, S_new), !.
conditions_met(P, S) :- subset(P, S).

member_state(S, [H|_]) :-	equal_set(S, H).
member_state(S, [_|T]) :-	member_state(S, T).

/* move types */

move(pickup(X), 
	[handempty, clear(X, R), on(X, Y, R), armroom(R)],
	[del(handempty), del(clear(X, R)), del(on(X, Y, R)),
		add(clear(Y, R)),	add(holding(X))]).

move(pickup(X), 
	[handempty, clear(X, R), ontable(X, R), armroom(R)],
	[del(handempty), del(clear(X, R)), del(ontable(X, R)),
		add(holding(X))]).

move(putdown(X), 
	[holding(X), armroom(R)],
	[del(holding(X)), add(ontable(X, R)), add(clear(X, R)),
		add(handempty)]).

move(stack(X, Y), 
	[holding(X), clear(Y, R), armroom(R)],
	[del(holding(X)), del(clear(Y, R)), add(handempty), add(on(X, Y, R)),
		add(clear(X, R))]).

/* Added movement commands to go to room1 and room2 */
move(goroom1,
	[armroom(2)],
	[del(armroom(2)), add(armroom(1))]).

move(goroom2,
	[armroom(1)],
	[del(armroom(1)), add(armroom(2))]).

/* run commands */

go(S, G) :- plan(S, G, [S], []).

test1 :- go([handempty, ontable(b, 1), ontable(c, 1), on(a, b, 1), clear(c, 1), clear(a, 1), armroom(1)],
	          [handempty, ontable(c, 1), on(a, b, 1), on(b, c, 1), clear(a, 1), armroom(1)]).

test2 :- go([handempty, ontable(b, 1), ontable(c, 1), on(a, b, 1), clear(c, 1), clear(a, 1), armroom(1)],
	          [handempty, ontable(b, 2), on(c, b, 2), on(a, c, 2), clear(a, 2), armroom(1)]).


