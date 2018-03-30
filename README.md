As of now:

- The program will set up graphing windows if the delta X field is specified and throws errors for when no "for x" value is given and no
expression with x is given
- I already implemented the functions for drawing the axis and the graphing a given double[] vector
Sam:
  - Please finish the CalculateYAxisValues() function - this will return a double[] that has the corresponding y values for 
    each value of x returned by CalculateXAxis values(). Right now, I have a dummy vector setup that should be deleted
  - the vectors returned by those two functions are already graphed 
  - Please implement the functions for clicking required in step 12. I have not started these yet. You may have to alter 
   the RefreshGraphPanel constructor from what I have to do this
   - as a heads up, here is how the graph window is structured so you can find the clicked points easier:
      
      Window title
      ______________________________________________________________
      |__________Top Margin_________________________________________<---this is a buffer zone
        |
        |
      y |
      a |   Pixles contained by the X and Y axis for graphing in
      x |
      i |
      s |
        |
        |____________________________________________________________
      ____________________________x-axis_____________________________
